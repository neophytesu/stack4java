package spring.ioc.bean.lifecycle.init;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {
}
