package id.unifi.service.common.api;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import com.google.common.reflect.ClassPath;
import com.statemachinesystems.envy.Envy;
import id.unifi.service.common.api.annotations.ApiConfigPrefix;
import id.unifi.service.common.api.annotations.ApiOperation;
import id.unifi.service.common.api.annotations.ApiService;
import id.unifi.service.common.config.UnifiConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceRegistry {
    private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);

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
                  String resultMessageType) {
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

    public ServiceRegistry(Map<String, String> packageNamesByModule, Map<Class<?>, Object> components) {
        ClassPath classPath;
        try {
            classPath = ClassPath.from(ServiceRegistry.class.getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException("Can't scan classpath, only system and URL-based class loaders supported.", e);
        }

        Map<String, Map<Class<?>, ApiService>> services = packageNamesByModule.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> discoverServices(classPath, e.getValue())));

        this.serviceInstances =
                createServiceInstances(new HashMap<>(components), services.values());
        this.operations = preloadOperations(services);
    }

    public Object invokeRpc(Operation operation, Object[] params) {
        try {
            return operation.method.invoke(serviceInstances.get(operation.cls), params);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public Operation getOperation(String messageType) {
        Operation operation = operations.get(messageType);
        if (operation == null) {
            throw new RuntimeException("Operation not found: " + messageType);
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

    private static Map<Class<?>, Object> createServiceInstances(Map<Class<?>, Object> components,
                                                                Collection<Map<Class<?>, ApiService>> services) {
        return services.stream()
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toMap(Function.identity(), cls -> {
                    log.debug("Creating service instance for {}", cls);
                    return createInstance(components, cls);
                }));
    }

    private static Object createInstance(Map<Class<?>, Object> components, Class<?> cls) {
        Constructor<?>[] constructors = cls.getConstructors();
        if (constructors.length != 1) {
            throw new RuntimeException("Expected one constructor, got " + constructors.length + " for " + cls);
        }
        Constructor<?> constructor = constructors[0];
        Object[] componentObjects = Arrays.stream(constructor.getParameters())
                .map(p -> getDependency(components, p))
                .toArray();
        try {
            Object instance = constructor.newInstance(componentObjects);
            components.put(cls, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object getDependency(Map<Class<?>, Object> components, Parameter parameter) {
        Class<?> type = parameter.getType();
        Object instance = components.get(type);
        if (instance == null) {
            if (type.isInterface()) {
                ApiConfigPrefix prefixAnnotation = parameter.getDeclaredAnnotation(ApiConfigPrefix.class);
                String prefix = prefixAnnotation == null ? null : prefixAnnotation.value();
                log.info("Getting configuration for {} with prefix {}", type, prefix);
                instance = Envy.configure(type, UnifiConfigSource.getForPrefix(prefix));
            } else {
                instance = createInstance(components, type);
            }
        }
        return instance;
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
                    String annotatedResultType = operationAnnotation.resultType();
                    String resultTypeName = annotatedResultType.isEmpty()
                            ? messageType + "-result"
                            : annotatedResultType.startsWith(".") ? operationNamespace + annotatedResultType : annotatedResultType;

                    Type returnType = method.getGenericReturnType();

                    Parameter[] methodParameters = method.getParameters();
                    Map<String, Type> params = new LinkedHashMap<>(methodParameters.length);
                    for (Parameter parameter : methodParameters) {
                        if (!parameter.isNamePresent()) {
                            throw new RuntimeException(
                                    "Method parameter names not found. Java compiler must be called with -parameter.");
                        }
                        params.put(parameter.getName(), parameter.getParameterizedType());
                    }
                    operations.put(messageType,
                            new Operation(cls, method, params, InvocationType.RPC, returnType, resultTypeName));
                }
            }
        }
        return operations;
    }
}
