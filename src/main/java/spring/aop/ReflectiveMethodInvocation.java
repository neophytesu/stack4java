package spring.aop;

import spring.aop.interfaces.MethodInterceptor;
import spring.aop.interfaces.MethodInvocation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class ReflectiveMethodInvocation implements MethodInvocation {
    Object target;
    Method method;
    Object[] arguments;
    List<MethodInterceptor> interceptors;
    int cursor;

    public ReflectiveMethodInvocation(Object target, Method method, Object[] args, List<MethodInterceptor> chain) {
        this.target = target;
        this.method = method;
        this.arguments = Objects.requireNonNullElseGet(args, () -> new Object[0]);
        this.interceptors = chain;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public Object proceed() throws Exception {
        if (cursor >= interceptors.size()) {
            method.setAccessible(true);
            return method.invoke(target, arguments);
        }
        MethodInterceptor interceptor = interceptors.get(cursor);
        cursor++;
        return interceptor.invoke(this);
    }
}
