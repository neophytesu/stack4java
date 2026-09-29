package mybatis;

import jdbc.template.JdbcTemplate;
import lombok.Getter;

import java.lang.reflect.Proxy;

public class SqlSession {
    @Getter
    private final JdbcTemplate jdbcTemplate;

    public SqlSession(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public <T> T getMapper(Class<T> type) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                new MapperProxy(this)
        );
    }
}
