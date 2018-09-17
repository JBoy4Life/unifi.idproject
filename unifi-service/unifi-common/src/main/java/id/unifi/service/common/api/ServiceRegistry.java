package id.unifi.service.common.api;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import com.google.common.reflect.ClassPath;
import static id.unifi.service.common.api.InvocationType.MULTI;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.annotations.HttpMatch;
import id.unifi.service.common.api.errors.UnknownMessageType;
import id.unifi.service.common.api.http.HttpSpec;
import static id.unifi.service.common.api.http.HttpUtils.decodePathSegments;
import id.unifi.service.common.api.http.PathNode;
import id.unifi.service.common.api.http.PathSegment;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableMap;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class ServiceRegistry {
    private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);
    private final ComponentHolder componentProvider;
    private final Map<HttpMethod, PathNode> operationUrlPathTree;

    public static class Operation {
        final Class<?> cls;
        final Map<String, Param> params;
        final InvocationType invocationType;
        final String resultMessageType;
        final Type resultType;
        final Method method;
        final HttpSpec httpSpec;

        private Operation(Class<?> cls,
                          Method method,
                          Map<String, Param> params,
                          InvocationType invocationType,
                          Type resultType,
                          @Nullable String resultMessageType,
                          HttpSpec httpSpec) {
            this.cls = cls;
            this.method = method;
            this.params = params;
            this.invocationType = invocationType;
            this.resultType = resultType;
            this.resultMessageType = resultMessageType;
            this.httpSpec = httpSpec;
        }
    }

    static class Param {
        final Type type;
        final boolean nullable;

        private Param(Type type, boolean nullable) {
            this.type = type;
            this.nullable = nullable;
        }
    }

    private final Map<Class<?>, Object> serviceInstances;
    private final Map<String, Operation> operations;

    public ServiceRegistry(Map<String, String> packageNamesByModule, ComponentHolder componentHolder) {
        ClassPath classPath;
        try {
            classPath = ClassPath.from(ServiceRegistry.class.getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException("Can't scan classpath, only system and URL-based class loaders supported.", e);
        }

        var services = packageNamesByModule.entrySet().stream()
                .collect(toUnmodifiableMap(Map.Entry::getKey, e -> discoverServices(classPath, e.getValue())));

        this.componentProvider = componentHolder;
        this.serviceInstances = createServiceInstances(services.values());
        this.operations = preloadOperations(services);
        this.operationUrlPathTree = generateUrlPathTree(operations.values());
    }

    public Object invokeRpc(Operation operation, Object[] params) {
        try {
            return operation.method.invoke(serviceInstances.get(operation.cls), params);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException() instanceof RuntimeException
                    ? (RuntimeException) e.getCause()
                    : new RuntimeException(e);
        }
    }

    public void invokeMulti(Operation operation, Object[] params, MessageListener<?> listenerParam) {
        var allParams = Stream.concat(stream(params), Stream.of(listenerParam)).toArray();
        try {
            operation.method.invoke(serviceInstances.get(operation.cls), allParams);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException() instanceof RuntimeException
                    ? (RuntimeException) e.getCause()
                    : new RuntimeException(e);
        }
    }

    public Operation getOperation(String messageType) {
        var operation = operations.get(messageType);
        if (operation == null) {
            throw new UnknownMessageType(messageType);
        }
        return operation;
    }

    public OperationMatch getOperationFromUrlPath(HttpMethod method, String urlEncodedPath) {
        var node = operationUrlPathTree.get(method);
        if (node == null) return null;

        var pathParams = new HashMap<String, String>();
        var segments = decodePathSegments(urlEncodedPath);
        for (var segment : segments) {
            var match = node.match(segment);
            if (match == null) return null;

            if (match.paramName != null) pathParams.put(match.paramName, match.paramValue);
            node = match.node;
        }

        var operation = node.getOperation();
        return operation == null ? null : new OperationMatch(operation, unmodifiableMap(pathParams));
    }

    private Map<HttpMethod, PathNode> generateUrlPathTree(Collection<Operation> operations) {
        var byMethod = new HashMap<HttpMethod, PathNode>();
        for (var operation : operations) {
            var httpSpec = operation.httpSpec;
            if (httpSpec == null) continue;
            var node = byMethod.computeIfAbsent(httpSpec.method, m -> new PathNode());
            for (var segment : httpSpec.segments) node = node.addMapping(segment);
            node.setOperation(operation);
        }

        return byMethod;
    }

    private static Map<Class<?>, ApiService> discoverServices(ClassPath classPath, String packageName) {
        Map<Class<?>, ApiService> classes = new HashMap<>();
        for (var classInfo : classPath.getTopLevelClasses(packageName)) {
            var cls = classInfo.load();
            var annotation = cls.getDeclaredAnnotation(ApiService.class);
            if (annotation != null) {
                classes.put(cls, annotation);
            }
        }
        return classes;
    }

    private Map<Class<?>, Object> createServiceInstances(Collection<Map<Class<?>, ApiService>> services) {
        return services.stream()
                .flatMap(map -> map.keySet().stream())
                .collect(toUnmodifiableMap(Function.identity(), cls -> {
                    log.debug("Creating service instance for {}", cls);
                    return componentProvider.get(cls);
                }));
    }

    private static Map<String, Operation> preloadOperations(Map<String, Map<Class<?>, ApiService>> services) {
        Map<String, Operation> operations = new HashMap<>();
        for (var module : services.entrySet()) {
            var moduleName = module.getKey();
            for (var service : module.getValue().entrySet()) {
                var cls = service.getKey();
                var serviceAnnotation = service.getValue();
                var serviceName = serviceAnnotation.value();
                var operationNamespace = moduleName + "." + serviceName;
                for (var method : cls.getDeclaredMethods()) {
                    var operationAnnotation = method.getAnnotation(ApiOperation.class);
                    if (operationAnnotation == null) continue;

                    var restAnnotation = method.getAnnotation(HttpMatch.class);
                    var operationName = operationAnnotation.name().isEmpty()
                            ? LOWER_CAMEL.to(LOWER_HYPHEN, method.getName())
                            : operationAnnotation.name();
                    var messageType = operationNamespace + "." + operationName;
                    var returnType = method.getGenericReturnType();
                    var methodParams = method.getParameters();

                    var multiReturnType = getMultiResponseReturnType(returnType, methodParams);
                    var invocationType = multiReturnType != null ? MULTI : InvocationType.RPC;
                    Map<String, Param> params;
                    switch (invocationType) {
                        case MULTI:
                            if (restAnnotation != null)
                                throw new RuntimeException("Unexpected " + HttpMatch.class +
                                        " annotation for multi-response operation: " + restAnnotation);
                            params = preloadParams(Arrays.copyOfRange(methodParams, 0, methodParams.length - 1));
                            operations.put(messageType,
                                    new Operation(cls, method, params, MULTI, multiReturnType, null, null));
                            break;
                        case RPC:
                            var annotatedResultType = operationAnnotation.resultType();
                            var resultTypeName = annotatedResultType.isEmpty()
                                    ? messageType + "-result"
                                    : annotatedResultType.startsWith(".") ? operationNamespace + annotatedResultType : annotatedResultType;

                            params = preloadParams(methodParams);

                            HttpSpec httpSpec;
                            if (restAnnotation != null) {
                                var path = restAnnotation.path();
                                var segments = stream(path.split("/"))
                                        .map(s -> (s.startsWith(":"))
                                                ? PathSegment.param(URLDecoder.decode(s.substring(1), UTF_8))
                                                : PathSegment.value(URLDecoder.decode(s, UTF_8)))
                                        .collect(toUnmodifiableList());
                                httpSpec = new HttpSpec(restAnnotation.method(), segments);
                            } else {
                                httpSpec = null;
                            }

                            operations.put(messageType, new Operation(
                                    cls, method, params, InvocationType.RPC, returnType, resultTypeName, httpSpec));
                            break;
                    }
                }
            }
        }
        return operations;
    }

    private static Map<String, Param> preloadParams(Parameter[] methodParameters) {
        Map<String, Param> params = new LinkedHashMap<>(methodParameters.length);
        for (var parameter : methodParameters) {
            if (!parameter.isNamePresent()) {
                throw new RuntimeException(
                        "Method parameter names not found. Java compiler must be called with -parameter.");
            }
            params.put(parameter.getName(), new Param(parameter.getParameterizedType(), parameter.isAnnotationPresent(Nullable.class)));
        }
        return params;
    }

    private static Type getMultiResponseReturnType(Type methodReturnType, Parameter[] methodParameters) {
        if (!methodReturnType.equals(Void.TYPE)) return null;

        var lastParamType = methodParameters[methodParameters.length - 1].getParameterizedType();
        if (!(lastParamType instanceof ParameterizedType)) return null;
        var type = (ParameterizedType) lastParamType;
        if (type.getRawType() != MessageListener.class) return null;
        return type.getActualTypeArguments()[0];
    }

    public static class OperationMatch {
        public final Operation operation;
        public final Map<String, String> pathParams;

        public OperationMatch(Operation operation, Map<String, String> pathParams) {
            this.operation = operation;
            this.pathParams = pathParams;
        }
    }
}
