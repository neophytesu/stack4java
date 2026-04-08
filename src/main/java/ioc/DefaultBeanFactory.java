package ioc;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultBeanFactory {
    ConcurrentHashMap<Class<?>, Object> singletonMap = new ConcurrentHashMap<>();
    Set<Class<?>> registeredTypes = new HashSet<>();

    public void register(Class<?> type) throws Exception {
        registeredTypes.add(type);
    }

    public void register(Object instance) {
        registeredTypes.add(instance.getClass());
        singletonMap.put(instance.getClass(), instance);
    }

    public Object getBean(Class<?> type) throws Exception {
        if (!registeredTypes.contains(type)) {
            return null;
        }
        return singletonMap.computeIfAbsent(type, clazz -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to create bean: " + clazz.getName(), e);
            }
        });
    }
}
