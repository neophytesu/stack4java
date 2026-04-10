package ioc.bean;

import ioc.annotation.Autowired;
import ioc.enums.BeanScope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultBeanFactory {
    ConcurrentHashMap<String, Object> singletonMap = new ConcurrentHashMap<>();

    private final BeanDefinitionRegistry beanDefinitionRegistry = new BeanDefinitionRegistry();

    public void register(Class<?> type) {
        beanDefinitionRegistry.registerBeanDefinition(type);
    }

    public void register(Object instance) {
        Class<?> clazz = instance.getClass();
        beanDefinitionRegistry.registerBeanDefinition(clazz);
        singletonMap.put(clazz.getName(), instance);
    }

    public Object getBean(String beanName) {
        BeanDefinition def = beanDefinitionRegistry.getBeanDefinition(beanName);
        if (def == null) {
            throw new IllegalStateException("不存在的bean" + beanName);
        }
        if (def.getScope() == BeanScope.SINGLETON) {
            return singletonMap.computeIfAbsent(def.getBeanName(), _ -> createBean(def));
        }
        if (def.getScope() == BeanScope.PROTOTYPE) {
            return createBean(def);
        }
        throw new UnsupportedOperationException("不支持的作用域");
    }

    public Object getBean(Class<?> type) {
        String[] names = beanDefinitionRegistry.getBeanNamesForType(type);
        int len = names.length;
        if (len == 0) {
            throw new IllegalStateException("不存在类型为" + type + "的Bean");
        }
        if (len == 1) {
            return getBean(names[0]);
        }
        int primaryCnt = 0;
        Object bean = null;
        for (String name : names) {
            BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(name);
            if (beanDefinition.isPrimary()) {
                bean = getBean(beanDefinition.getBeanName());
                if (primaryCnt++ > 0) {
                    throw new IllegalStateException(type.getName() + "类型存在多个primary类");
                }
            }
        }
        if (bean != null) {
            return bean;
        }
        throw new IllegalStateException(type.getName() + "存在多个bean定义");
    }

    private Object createBean(BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getBeanClass();
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
                instance = clazz.getDeclaredConstructor().newInstance();
            }
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(instance, getBean(field.getType()));
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + clazz.getName(), e);
        }
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
