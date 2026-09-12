package spring.aop.pointcut;

import spring.service.annotations.Transactional;

import java.lang.reflect.Method;

public class TransactionalPointcut implements Pointcut {
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (method.isAnnotationPresent(Transactional.class)) {
            return true;
        }
        return targetClass.isAnnotationPresent(Transactional.class);
    }
}
