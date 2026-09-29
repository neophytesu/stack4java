package mybatis.interceptor;

public class LogInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Exception {
        IO.println("SQL " + invocation.name() + " " + invocation.args()[0]);
        return invocation.proceed();
    }
}
