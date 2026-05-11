package spring.aop.pointcut;

import spring.service.annotations.LogMethod;

import java.lang.reflect.Method;

public class LogMethodPointcut implements Pointcut {
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (method.isAnnotationPresent(LogMethod.class)) {
            return true;
        }
        return targetClass.isAnnotationPresent(LogMethod.class);
    }
}
