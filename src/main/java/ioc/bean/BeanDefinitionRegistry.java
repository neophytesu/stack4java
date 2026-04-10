package ioc.bean;

import ioc.annotation.Primary;
import ioc.annotation.Scope;
import ioc.enums.BeanScope;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BeanDefinitionRegistry {
    private final ConcurrentHashMap<String, BeanDefinition> registeredBeans = new ConcurrentHashMap<>();

    void registerBeanDefinition(String beanName, BeanDefinition definition) {
        if (containsBeanDefinition(beanName)) {
            throw new RuntimeException("Bean with the same name is registered!");
        }
        if (definition.getScope() == null) {
            definition.setScope(BeanScope.SINGLETON);
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
        if (clazz.isAnnotationPresent(Scope.class)) {
            beanDefinition.setScope(clazz.getAnnotation(Scope.class).value());
        } else {
            beanDefinition.setScope(BeanScope.SINGLETON);
        }
        if (clazz.isAnnotationPresent(Primary.class)) {
            beanDefinition.setPrimary(true);
        }
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

    public String[] getBeanNamesForType(Class<?> type) {
        List<String> beanNames = new ArrayList<>();
        registeredBeans.forEach((beanName, beanDefinition) -> {
            if (type.isAssignableFrom(beanDefinition.getBeanClass())) {
                beanNames.add(beanName);
            }
        });
        return beanNames.toArray(new String[0]);
    }
}
