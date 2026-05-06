package spring.core;

import spring.aop.BeanEnhancer;
import spring.aop.interfaces.MethodInterceptor;
import spring.di.annotation.Autowired;
import spring.ioc.bean.BeanDefinition;
import spring.ioc.bean.lifecycle.BeanPostProcessor;
import spring.ioc.bean.lifecycle.aware.BeanFactoryAware;
import spring.ioc.bean.lifecycle.aware.BeanNameAware;
import spring.ioc.bean.lifecycle.init.InitializingBean;
import spring.ioc.bean.lifecycle.init.PostConstruct;
import spring.ioc.enums.BeanScope;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultBeanFactory {
    ConcurrentHashMap<String, Object> singletonMap = new ConcurrentHashMap<>();

    private final BeanDefinitionRegistry beanDefinitionRegistry = new BeanDefinitionRegistry();

    private final BeanEnhancer beanEnhancer = new BeanEnhancer();

    List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public void addInterceptors(List<MethodInterceptor> interceptors) {
        interceptors.forEach(beanEnhancer::addInterceptor);
    }

    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        beanPostProcessors.add(beanPostProcessor);
    }

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
            if (!singletonMap.containsKey(beanName)) {
                Object rawBean = doCreateBean(def);
                registerDisposableBeanIfNecessary(beanName, rawBean);
                singletonMap.put(def.getBeanName(), beanEnhancer.enhance(rawBean));
            }
        }
        if (def.getScope() == BeanScope.PROTOTYPE) {
            return beanEnhancer.enhance(doCreateBean(def));
        }
        throw new UnsupportedOperationException("不支持的作用域");
    }

    private void registerDisposableBeanIfNecessary(String beanName, Object rawBean) {

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

    private Object doCreateBean(BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getBeanClass();
        String beanName = beanDefinition.getBeanName();
        Object instance;
        try {
            instance = instantiateBean(clazz);
            populateBean(instance, clazz);
            invokeAwareMethod(instance, beanName);
            instance = applyBeanPostProcessorsBeforeInitialization(instance, beanName);
            initializeBean(instance, clazz);
            if (instance instanceof InitializingBean ib) {
                ib.afterPropertiesSet();
            }
            instance = applyBeanPostProcessorsAfterInitialization(instance, beanName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + clazz.getName(), e);
        }
        return instance;
    }

    private Object instantiateBean(Class<?> clazz) throws Exception {
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
        return instance;
    }

    private void populateBean(Object instance, Class<?> clazz) throws IllegalAccessException {
        if (clazz != Object.class) {
            populateBean(instance, clazz.getSuperclass());
        } else {
            return;
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                field.set(instance, getBean(field.getType()));
            }
        }
    }

    private void invokeAwareMethod(Object instance, String beanName) {
        if (instance instanceof BeanNameAware bn) {
            bn.setBeanName(beanName);
        }
        if (instance instanceof BeanFactoryAware bf) {
            bf.setBeanFactory(this);
        }
    }

    private Object applyBeanPostProcessorsBeforeInitialization(Object instance, String beanName) {
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            instance = beanPostProcessor.before(instance, beanName);
        }
        return instance;
    }

    private void initializeBean(Object instance, Class<?> clazz) throws Exception {
        if (clazz != Object.class) {
            initializeBean(instance, clazz.getSuperclass());
        } else {
            return;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                method.setAccessible(true);
                if (method.getParameterTypes().length == 0) {
                    method.invoke(instance);
                }
                break;
            }
        }
    }

    private Object applyBeanPostProcessorsAfterInitialization(Object instance, String beanName) {
        for (int i = beanPostProcessors.size() - 1; i >= 0; i--) {
            instance = beanPostProcessors.get(i).after(instance, beanName);
        }
        return instance;
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
