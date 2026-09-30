package spring.ioc.bean;

import spring.ioc.enums.BeanScope;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@Builder
public class BeanDefinition {

    private String beanName;
    private Class<?> beanClass;
    private BeanScope scope;
    private boolean primary;
    private String factoryBeanName;
    private Method factoryMethod;
    private String qualifier;
}
