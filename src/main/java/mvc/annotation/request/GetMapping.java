package mvc.annotation.request;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@RequestMapping("GET")
public @interface GetMapping {
    String value() default "";
}
