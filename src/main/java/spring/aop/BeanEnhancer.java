package spring.aop;

import spring.aop.advisor.Advisor;
import spring.aop.advisor.SimpleAdvisor;
import spring.aop.interceptor.MethodInterceptor;
import spring.aop.interceptor.MethodInvocation;
import spring.aop.pointcut.Pointcut;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class BeanEnhancer {

    private final List<Advisor> advisors = new ArrayList<>();

    public void addAdvisor(Advisor advisor) {
        advisors.add(advisor);
    }

    public void addInterceptor(MethodInterceptor methodInterceptor) {
        advisors.add(new SimpleAdvisor((_, _) -> true, methodInterceptor));
    }

    public Object enhance(Object raw) {
        Object exposed = raw;
        if (needsProxy(raw.getClass())) {
            exposed = createJdkProxy(raw, advisors);
        }
        return exposed;
    }

    private boolean needsProxy(Class<?> beanClass) {
        if (advisors.isEmpty()) {
            return false;
        }
        if (beanClass.isInterface()) {
            return false;
        }
        for (Class<?> i : beanClass.getInterfaces()) {
            for (Method method : i.getDeclaredMethods()) {
                int mod = method.getModifiers();
                if (Modifier.isStatic(mod)) {
                    continue;
                }
                if (method.isBridge() || method.isSynthetic()) {
                    continue;
                }
                for (Advisor advisor : advisors) {
                    if (advisor.getPointcut().matches(method, beanClass)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Object createJdkProxy(Object target, List<Advisor> advisors) {
        ClassLoader cl = target.getClass().getClassLoader();
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalStateException("无接口无法进行JDK代理");
        }
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(target, args);
            }
            List<MethodInterceptor> chain = new ArrayList<>();
            for (Advisor advisor : advisors) {
                if (advisor.getPointcut().matches(method, target.getClass())) {
                    chain.add(advisor.getInterceptor());
                }
            }
            if (chain.isEmpty()) {
                method.setAccessible(true);
                return method.invoke(target, args);
            }
            MethodInvocation mi = new ReflectiveMethodInvocation(target, method, args, chain);
            return mi.proceed();
        };
        return Proxy.newProxyInstance(cl, interfaces, handler);
    }
}
