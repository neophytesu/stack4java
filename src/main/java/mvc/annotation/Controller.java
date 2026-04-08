package mvc.annotation;

import ioc.annotation.Bean;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Bean
public @interface Controller {
    String value() default "";
}
