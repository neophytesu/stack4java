package spring.aop;

import spring.service.annotations.LogMethod;
import spring.aop.interfaces.MethodInterceptor;
import spring.aop.interfaces.MethodInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class BeanEnhancer {

    private final List<MethodInterceptor> interceptorList = new ArrayList<>();

    public void addInterceptor(MethodInterceptor interceptor) {
        interceptorList.add(interceptor);
    }

    public Object enhance(Object raw) {
        Object exposed = raw;
        if (needsProxy(raw.getClass())) {
            exposed = createJdkProxy(raw, interceptorList);
        }
        return exposed;
    }

    private boolean needsProxy(Class<?> beanClass) {
        if (beanClass.isInterface()) {
            return false;
        }
        if (beanClass.getInterfaces().length == 0) {
            return false;
        }
        return beanClass.isAnnotationPresent(LogMethod.class);
    }

    private Object createJdkProxy(Object target, List<MethodInterceptor> chain) {
        ClassLoader cl = target.getClass().getClassLoader();
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalStateException("无接口无法进行JDK代理");
        }
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(target, args);
            }
            MethodInvocation mi = new ReflectiveMethodInvocation(target, method, args, chain);
            return mi.proceed();
        };
        return Proxy.newProxyInstance(cl, interfaces, handler);
    }
}
