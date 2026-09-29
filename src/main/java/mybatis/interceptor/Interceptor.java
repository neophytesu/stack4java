package mybatis.interceptor;

public interface Interceptor {
    Object intercept(Invocation invocation) throws Exception;
}
