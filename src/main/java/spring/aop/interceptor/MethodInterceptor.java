package spring.aop.interceptor;

public interface MethodInterceptor {
    Object invoke(MethodInvocation invocation);
}
