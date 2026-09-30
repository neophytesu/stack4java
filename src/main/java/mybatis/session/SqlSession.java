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
    private final SqlSessionFactory factory;

    public SqlSession(JdbcTemplate jdbcTemplate, InterceptorChain interceptorChain, SqlSessionFactory factory) {
        this.jdbcTemplate = jdbcTemplate;
        this.interceptorChain = interceptorChain;
        this.factory = factory;
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

    public <T> T selectOne(String sql, Object[] args, RowMapper<T> mapper, String namespace, boolean cache) {
        return (T) invoke("selectOne", new Object[]{sql, args, mapper, namespace, cache});
    }

    public <T> T doSelectOne(String sql, Object[] args, RowMapper<T> mapper, String namespace, Boolean cache) {
        String key = key("one", sql, args);
        if (localCache.containsKey(key)) {
            return (T) localCache.get(key);
        }
        if (cache && factory.hasL2(namespace, key)) {
            T hit = (T) factory.getL2(namespace, key);
            localCache.put(key, hit);
            return hit;
        }
        T result = jdbcTemplate.queryForObject(sql, args, mapper);
        localCache.put(key, result);
        if (cache && result != null) {
            factory.putL2(namespace, key, result);
        }
        return result;
    }

    public <T> List<T> selectList(String sql, Object[] args, RowMapper<T> mapper, String namespace, boolean cache) {
        return (List<T>) invoke("selectList", new Object[]{sql, args, mapper, namespace, cache});
    }

    public <T> List<T> doSelectList(String sql, Object[] args, RowMapper<T> mapper, String namespace, boolean cache) {
        String key = key("list", sql, args);
        if (localCache.containsKey(key)) {
            return (List<T>) localCache.get(key);
        }
        if (cache && factory.hasL2(namespace, key)) {
            List<T> hit = (List<T>) factory.getL2(namespace, key);
            localCache.put(key, hit);
            return hit;
        }
        List<T> result = jdbcTemplate.query(sql, args, mapper);
        localCache.put(key, result);
        if (cache && result != null) {
            factory.putL2(namespace, key, result);
        }
        return result;
    }

    public int doUpdate(String sql, Object[] args, String namespace, boolean cache) {
        localCache.clear();
        if (cache) {
            factory.clearL2(namespace);
        }
        return jdbcTemplate.update(sql, args);
    }

    public int doUpdateAndReturnKey(String sql, Object[] args, String namespace, boolean cache) {
        localCache.clear();
        if (cache) {
            factory.clearL2(namespace);
        }
        return jdbcTemplate.updateAndReturnKey(sql, args);
    }

    public int updateAndReturnKey(String sql, Object[] args, String namespace, boolean cache) {
        return (Integer) invoke("updateAndReturnKey", new Object[]{sql, args, namespace, cache});
    }

    public int update(String sql, Object[] args, String namespace, boolean cache) {
        return (Integer) invoke("update", new Object[]{sql, args, namespace, cache});
    }

    private static String key(String kind, String sql, Object[] args) {
        return kind + "|" + sql + "|" + Arrays.deepToString(args);
    }
}
