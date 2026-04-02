package mvc.annotation.request;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@RequestMapping("POST")
public @interface PutMapping {
    String value() default "";
}
