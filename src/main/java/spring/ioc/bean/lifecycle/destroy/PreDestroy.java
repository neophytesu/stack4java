package spring.ioc.bean.lifecycle.destroy;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface PreDestroy {
}
