package mybatis.session;

import jdbc.RowMapper;
import jdbc.template.JdbcTemplate;
import lombok.Getter;
import mybatis.interceptor.InterceptorChain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlSession {
    @Getter
    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Object> localCache = new HashMap<>();
    private final InterceptorChain interceptorChain;

    public SqlSession(JdbcTemplate jdbcTemplate, InterceptorChain interceptorChain) {
        this.jdbcTemplate = jdbcTemplate;
        this.interceptorChain = interceptorChain;
    }

    public void close() {
        localCache.clear();
    }

    private Object invoke(String name, Object[] args) {
        try {
            return interceptorChain.invoke(this, name, args);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T selectOne(String sql, Object[] args, RowMapper<T> mapper) {
        return (T) invoke("selectOne", new Object[]{sql, args, mapper});
    }

    public <T> T doSelectOne(String sql, Object[] args, RowMapper<T> mapper) {
        String key = key("one", sql, args);
        if (localCache.containsKey(key)) {
            return (T) localCache.get(key);
        }
        T result = jdbcTemplate.queryForObject(sql, args, mapper);
        localCache.put(key, result);
        return result;
    }

    public <T> List<T> selectList(String sql, Object[] args, RowMapper<T> mapper) {
        return (List<T>) invoke("selectList", new Object[]{sql, args, mapper});
    }

    public <T> List<T> doSelectList(String sql, Object[] args, RowMapper<T> mapper) {
        String key = key("list", sql, args);
        if (localCache.containsKey(key)) {
            return (List<T>) localCache.get(key);
        }
        List<T> result = jdbcTemplate.query(sql, args, mapper);
        localCache.put(key, result);
        return result;
    }

    public int doUpdate(String sql, Object[] args) {
        localCache.clear();
        return jdbcTemplate.update(sql, args);
    }

    public int doUpdateAndReturnKey(String sql, Object[] args) {
        localCache.clear();
        return jdbcTemplate.updateAndReturnKey(sql, args);
    }

    public int updateAndReturnKey(String sql, Object[] args) {
        return (Integer) invoke("updateAndReturnKey", new Object[]{sql, args});
    }

    public int update(String sql, Object[] args) {
        return (Integer) invoke("update", new Object[]{sql, args});
    }

    private static String key(String kind, String sql, Object[] args) {
        return kind + "|" + sql + "|" + Arrays.deepToString(args);
    }
}
