package spring.aop.advisor;

import lombok.AllArgsConstructor;
import spring.aop.interceptor.MethodInterceptor;
import spring.aop.pointcut.Pointcut;

@AllArgsConstructor
public class SimpleAdvisor implements Advisor {

    private Pointcut pointcut;
    private MethodInterceptor methodInterceptor;

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public MethodInterceptor getInterceptor() {
        return methodInterceptor;
    }
}
