package spring.aop.advisor;

import spring.aop.interceptor.MethodInterceptor;
import spring.aop.pointcut.Pointcut;

public interface Advisor {
    Pointcut getPointcut();

    MethodInterceptor getInterceptor();
}
