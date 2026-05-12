package spring.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import spring.aop.advisor.Advisor;
import spring.aop.advisor.SimpleAdvisor;
import spring.aop.interceptor.MethodInterceptor;
import spring.aop.interceptor.MethodInvocation;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class BeanEnhancer {

    private final List<Advisor> advisors = new ArrayList<>();
    private final ConcurrentHashMap<Fingerprint, Class<?>> cache = new ConcurrentHashMap<>();

    public void addAdvisor(Advisor advisor) {
        advisors.add(advisor);
    }

    public void addInterceptor(MethodInterceptor methodInterceptor) {
        advisors.add(new SimpleAdvisor((_, _) -> true, methodInterceptor));
    }

    public Object enhance(Object target) throws Exception {
        Object exposed = target;
        if (needProxy(target.getClass())) {
            if (preferJdkProxy(target.getClass())) {
                exposed = createJdkProxy(target, advisors);
            } else {
                if (!Modifier.isFinal(target.getClass().getModifiers())) {
                    exposed = createBuddyProxy(target, advisors);
                }
            }
        }
        return exposed;
    }

    private boolean needProxy(Class<?> beanClass) {
        if (advisors.isEmpty()) {
            return false;
        }
        if (advisorMatchMethods(beanClass.getDeclaredMethods(), beanClass)) {
            return true;
        }
        return advisorsMatchInterfaces(beanClass);
    }

    private boolean preferJdkProxy(Class<?> beanClass) {
        if (beanClass.isInterface()) {
            return false;
        }
        return advisorsMatchInterfaces(beanClass);
    }

    private boolean advisorsMatchInterfaces(Class<?> beanClass) {
        for (Class<?> i : beanClass.getInterfaces()) {
            if (advisorMatchMethods(i.getDeclaredMethods(), beanClass)) {
                return true;
            }
        }
        return false;
    }

    private boolean advisorMatchMethods(Method[] methods, Class<?> beanClass) {
        for (Method method : methods) {
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
        return false;
    }


    private Object createJdkProxy(Object target, List<Advisor> advisors) {
        ClassLoader cl = target.getClass().getClassLoader();
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalStateException("无接口无法进行JDK代理");
        }
        InvocationHandler handler = (_, method, args) -> {
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

    private Object createBuddyProxy(Object target, List<Advisor> advisors) throws Exception {
        Class<?> targetClass = target.getClass();
        Fingerprint fp = new Fingerprint(targetClass, System.identityHashCode(targetClass.getClassLoader()), advisorsDigest(advisors));
        Class<?> loaded = cache.computeIfAbsent(fp, _ -> {
            try {
                try (DynamicType.Unloaded<?> unload = new ByteBuddy()
                        .subclass(targetClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                        .defineField("target", Object.class, Modifier.PRIVATE | Modifier.FINAL)
                        .defineField("advisors", List.class, Modifier.PRIVATE | Modifier.FINAL)
                        .defineConstructor(Modifier.PUBLIC)
                        .withParameters(Object.class, List.class)
                        .intercept(MethodCall.invoke(targetClass.getDeclaredConstructor()).onSuper()
                                .andThen(FieldAccessor.ofField("target").setsArgumentAt(0))
                                .andThen(FieldAccessor.ofField("advisors").setsArgumentAt(1)))
                        .method(isDeclaredBy(targetClass).and(isVirtual()).and(not(isFinal())))
                        .intercept(MethodDelegation.to(AopDelegator.class))
                        .make()) {
                    return unload.load(targetClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                            .getLoaded();
                }
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
        Constructor<?> ctor = loaded.getDeclaredConstructor(Object.class, List.class);
        ctor.setAccessible(true);
        return ctor.newInstance(target, List.copyOf(advisors));
    }

    private long advisorsDigest(List<Advisor> advisors) {
        long h = 17;
        for (Advisor advisor : advisors) {
            h = h * 31 + Objects.hashCode(advisor.getClass().getName());
            h = h * 31 + System.identityHashCode(advisor.getPointcut());
            h = h * 31 + System.identityHashCode(advisor.getInterceptor());
        }
        return h;
    }
}
