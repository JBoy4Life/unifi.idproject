package id.unifi.service.common.api;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import com.google.common.reflect.ClassPath;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.api.errors.UnknownMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceRegistry {
    private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);
    private final ComponentHolder componentProvider;

    static class Operation {
        final Class<?> cls;
        final Map<String, Type> params;
        final InvocationType invocationType;
        final String resultMessageType;
        final Type resultType;
        final Method method;

        Operation(Class<?> cls,
                  Method method,
                  Map<String, Type> params,
                  InvocationType invocationType,
                  Type resultType,
                  @Nullable String resultMessageType) {
            this.cls = cls;
            this.method = method;
            this.params = params;
            this.invocationType = invocationType;
            this.resultType = resultType;
            this.resultMessageType = resultMessageType;
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

        Map<String, Map<Class<?>, ApiService>> services = packageNamesByModule.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> discoverServices(classPath, e.getValue())));

        this.componentProvider = componentHolder;
        this.serviceInstances = createServiceInstances(services.values());
        this.operations = preloadOperations(services);
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
        Object[] allParams = Stream.concat(Arrays.stream(params), Stream.of(listenerParam)).toArray();
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
        Operation operation = operations.get(messageType);
        if (operation == null) {
            throw new UnknownMessageType(messageType);
        }
        return operation;
    }

    private static Map<Class<?>, ApiService> discoverServices(ClassPath classPath, String packageName) {
        Map<Class<?>, ApiService> classes = new HashMap<>();
        for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClasses(packageName)) {
            Class<?> cls = classInfo.load();
            ApiService annotation = cls.getDeclaredAnnotation(ApiService.class);
            if (annotation != null) {
                classes.put(cls, annotation);
            }
        }
        return classes;
    }

    private Map<Class<?>, Object> createServiceInstances(Collection<Map<Class<?>, ApiService>> services) {
        return services.stream()
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toMap(Function.identity(), cls -> {
                    log.debug("Creating service instance for {}", cls);
                    return componentProvider.get(cls);
                }));
    }

    private static Map<String, Operation> preloadOperations(Map<String, Map<Class<?>, ApiService>> services) {
        Map<String, Operation> operations = new HashMap<>();
        for (Map.Entry<String, Map<Class<?>, ApiService>> module : services.entrySet()) {
            String moduleName = module.getKey();
            for (Map.Entry<Class<?>, ApiService> service : module.getValue().entrySet()) {
                Class<?> cls = service.getKey();
                ApiService serviceAnnotation = service.getValue();
                String serviceName = serviceAnnotation.value();
                String operationNamespace = moduleName + "." + serviceName;
                for (Method method : cls.getDeclaredMethods()) {
                    ApiOperation operationAnnotation = method.getAnnotation(ApiOperation.class);
                    if (operationAnnotation == null) continue;

                    String operationName = operationAnnotation.name().isEmpty()
                            ? LOWER_CAMEL.to(LOWER_HYPHEN, method.getName())
                            : operationAnnotation.name();
                    String messageType = operationNamespace + "." + operationName;
                    Type returnType = method.getGenericReturnType();
                    Parameter[] methodParams = method.getParameters();

                    Type multiReturnType = getMultiResponseReturnType(returnType, methodParams);
                    InvocationType invocationType =
                            multiReturnType != null ? InvocationType.MULTI : InvocationType.RPC;
                    Map<String, Type> params;
                    switch (invocationType) {
                        case MULTI:
                            params = preloadParams(Arrays.copyOfRange(methodParams, 0, methodParams.length - 1));
                            operations.put(messageType,
                                    new Operation(cls, method, params, InvocationType.MULTI, multiReturnType, null));
                            break;
                        case RPC:
                            String annotatedResultType = operationAnnotation.resultType();
                            String resultTypeName = annotatedResultType.isEmpty()
                                    ? messageType + "-result"
                                    : annotatedResultType.startsWith(".") ? operationNamespace + annotatedResultType : annotatedResultType;

                            params = preloadParams(methodParams);
                            operations.put(messageType,
                                    new Operation(cls, method, params, InvocationType.RPC, returnType, resultTypeName));
                            break;
                    }
                }
            }
        }
        return operations;
    }

    private static Map<String, Type> preloadParams(Parameter[] methodParameters) {
        Map<String, Type> params = new LinkedHashMap<>(methodParameters.length);
        for (Parameter parameter : methodParameters) {
            if (!parameter.isNamePresent()) {
                throw new RuntimeException(
                        "Method parameter names not found. Java compiler must be called with -parameter.");
            }
            params.put(parameter.getName(), parameter.getParameterizedType());
        }
        return params;
    }

    private static Type getMultiResponseReturnType(Type methodReturnType, Parameter[] methodParameters) {
        if (!methodReturnType.equals(Void.TYPE)) return null;

        Type lastParamType = methodParameters[methodParameters.length - 1].getParameterizedType();
        if (!(lastParamType instanceof ParameterizedType)) return null;
        ParameterizedType type = (ParameterizedType) lastParamType;
        if (type.getRawType() != MessageListener.class) return null;
        return type.getActualTypeArguments()[0];
    }
}
