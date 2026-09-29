package mybatis.session;

import jdbc.RowMapper;
import jdbc.template.JdbcTemplate;
import lombok.Getter;
import mybatis.mapper.MapperProxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlSession {
    @Getter
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Object> localCache = new HashMap<>();

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
        String key = key("one", sql, args);
        if (localCache.containsKey(key)) {
            return (T) localCache.get(key);
        }
        T result = jdbcTemplate.queryForObject(sql, args, mapper);
        localCache.put(key, result);
        return result;
    }

    public <T> List<T> selectList(String sql, Object[] args, RowMapper<T> mapper) {
        String key = key("list", sql, args);
        if (localCache.containsKey(key)) {
            return (List<T>) localCache.get(key);
        }
        List<T> result = jdbcTemplate.query(sql, args, mapper);
        localCache.put(key, result);
        return result;
    }

    public int update(String sql, Object[] args) {
        localCache.clear();
        return jdbcTemplate.update(sql, args);
    }

    private static String key(String kind, String sql, Object[] args) {
        return kind + "|" + sql + "|" + Arrays.deepToString(args);
    }
}
