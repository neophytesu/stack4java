package spring.aop;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import spring.aop.advisor.Advisor;
import spring.aop.interceptor.MethodInterceptor;
import spring.aop.interceptor.MethodInvocation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AopDelegator {


    @RuntimeType
    public static Object intercept(@Origin Method method, @AllArguments Object[] args, @FieldValue("target") Object target, @FieldValue("advisors") List<Advisor> advisors) throws Exception {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(target, args);
        }
        List<MethodInterceptor> chain = new ArrayList<>();
        Class<?> targerClass = target.getClass();
        for (Advisor advisor : advisors) {
            if (advisor.getPointcut().matches(method, targerClass)) {
                chain.add(advisor.getInterceptor());
            }
        }
        if (chain.isEmpty()) {
            method.setAccessible(true);
            return method.invoke(target, args);
        }
        MethodInvocation mi = new ReflectiveMethodInvocation(target, method, args, chain);
        return mi.proceed();
    }
}
