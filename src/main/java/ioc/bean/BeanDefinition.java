package ioc.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BeanDefinition {

    private String beanName;
    private Class<?> beanClass;
    private String scope;
    private boolean primary;
}
