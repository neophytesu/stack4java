package spring.ioc.annotation;

import spring.ioc.enums.BeanScope;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {
    BeanScope value() default BeanScope.SINGLETON;
}
