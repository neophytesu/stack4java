package spring.aop.interceptor;

import spring.aop.interfaces.MethodInterceptor;
import spring.aop.interfaces.MethodInvocation;

import java.lang.reflect.InvocationTargetException;

public class LogMethodInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) {
        String methodName = invocation.getMethod().getName();
        System.out.println(methodName + "被调用了");
        try {
            return invocation.proceed();
        } catch (Exception e) {
            System.out.println(methodName + "调用失败");
            if (e instanceof InvocationTargetException t) {
                throw new RuntimeException(t.getTargetException());
            }
            throw new RuntimeException(e);
        } finally {
            System.out.println(methodName + "调用结束");
        }
    }
}
