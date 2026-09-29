package mybatis.session;

import jdbc.RowMapper;
import jdbc.template.JdbcTemplate;
import lombok.Getter;
import mybatis.mapper.MapperProxy;

import java.lang.reflect.Proxy;
import java.util.List;

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

    public <T> T selectOne(String sql, Object[] args, RowMapper<T> mapper) {
        return jdbcTemplate.queryForObject(sql, args, mapper);
    }

    public <T> List<T> selectList(String sql, Object[] args, RowMapper<T> mapper) {
        return jdbcTemplate.query(sql, args, mapper);
    }

    public int update(String sql, Object[] args) {
        return jdbcTemplate.update(sql, args);
    }
}
