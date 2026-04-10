package ioc.bean;

import ioc.enums.BeanScope;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeanDefinition {

    private String beanName;
    private Class<?> beanClass;
    private BeanScope scope;
    private boolean primary;
}
