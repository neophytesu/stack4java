package spring.aop.interfaces;

public interface MethodInterceptor {
    Object invoke(MethodInvocation invocation);
}
