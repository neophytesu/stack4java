package mybatis.session;

import jdbc.template.JdbcTemplate;
import mybatis.interceptor.Interceptor;
import mybatis.interceptor.InterceptorChain;
import mybatis.mapper.MapperProxy;

import java.lang.reflect.Proxy;
import java.util.List;

public class SqlSessionFactory {
    private final JdbcTemplate jdbcTemplate;
    private final List<Interceptor> interceptors;

    public SqlSessionFactory(JdbcTemplate jdbcTemplate, List<Interceptor> interceptors) {
        this.jdbcTemplate = jdbcTemplate;
        this.interceptors = List.copyOf(interceptors);
    }

    public SqlSession openSession() {
        return new SqlSession(jdbcTemplate, new InterceptorChain(interceptors));
    }

    public <T> T getMapper(Class<T> type) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                new MapperProxy(this)
        );
    }
}
