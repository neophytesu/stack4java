package ioc.bean;

import ioc.annotation.Autowired;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultBeanFactory {
    ConcurrentHashMap<Class<?>, Object> singletonMap = new ConcurrentHashMap<>();

    private final BeanDefinitionRegistry beanDefinitionRegistry = new BeanDefinitionRegistry();

    public void register(Class<?> type) throws Exception {
        beanDefinitionRegistry.registerBeanDefinition(type);
    }

    public void register(Object instance) {
        beanDefinitionRegistry.registerBeanDefinition(instance.getClass());
        singletonMap.put(instance.getClass(), instance);
    }

    public Object getBean(Class<?> type) {
        if (type.isPrimitive()) {
            throw new RuntimeException("unsupported primitive type");
        }
        if (!beanDefinitionRegistry.containsBeanDefinition(type.getName())) {
            throw new IllegalStateException("Unknown bean type: " + type);
        }
        return singletonMap.computeIfAbsent(type, clazz -> {
            try {
                Object instance = null;
                int autowiredCnt = 0;
                for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                    if (constructor.isAnnotationPresent(Autowired.class)) {
                        if (autowiredCnt++ > 0) {
                            throw new IllegalStateException("too many autowired constructor!");
                        }
                        constructor.setAccessible(true);
                        instance = createByConstructor(constructor);
                    }
                }
                if (instance == null && clazz.getDeclaredConstructors().length == 1) {
                    Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                    constructor.setAccessible(true);
                    instance = createByConstructor(constructor);
                }
                if (instance == null) {
                    instance = type.getDeclaredConstructor().newInstance();
                }
                for (Field field : type.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Autowired.class)) {
                        field.setAccessible(true);
                        field.set(instance, getBean(field.getType()));
                    }
                }
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create bean: " + clazz.getName(), e);
            }
        });
    }

    private Object createByConstructor(Constructor<?> constructor) throws Exception {
        List<Object> params = new ArrayList<>();
        for (Class<?> parameterType : constructor.getParameterTypes()) {
            Object dep = getBean(parameterType);
            if (dep == null) {
                throw new IllegalStateException("No bean for " + parameterType + " required by " + constructor);
            }
            params.add(dep);
        }
        return constructor.newInstance(params.toArray());
    }
}
