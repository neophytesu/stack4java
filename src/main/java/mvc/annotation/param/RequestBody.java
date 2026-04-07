package mvc.annotation.param;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface RequestBody {
    boolean required() default true;
}
