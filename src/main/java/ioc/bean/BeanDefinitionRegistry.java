package ioc.bean;

import java.util.concurrent.ConcurrentHashMap;

public class BeanDefinitionRegistry {
    private final ConcurrentHashMap<String, BeanDefinition> registeredBeans = new ConcurrentHashMap<>();

    void registerBeanDefinition(String beanName, BeanDefinition definition) {
        if (containsBeanDefinition(beanName)) {
            throw new RuntimeException("Bean with the same name is registered!");
        }
        registeredBeans.put(beanName, definition);
    }

    void registerBeanDefinition(Class<?> clazz) {
        if (containsBeanDefinition(clazz.getName())) {
            throw new RuntimeException("Bean with the same name is registered!");
        }
        BeanDefinition beanDefinition = BeanDefinition.builder()
                .beanName(clazz.getName())
                .beanClass(clazz)
                .build();
        registeredBeans.put(clazz.getName(), beanDefinition);
    }

    void removeBeanDefinition(String beanName) {
        registeredBeans.remove(beanName);
    }

    boolean containsBeanDefinition(String beanName) {
        return registeredBeans.containsKey(beanName);
    }

    BeanDefinition getBeanDefinition(String beanName) {
        return registeredBeans.get(beanName);
    }

    String[] getBeanDefinitionNames() {
        if (registeredBeans.isEmpty()) {
            return new String[0];
        }
        return registeredBeans.keySet().toArray(new String[0]);
    }
}
