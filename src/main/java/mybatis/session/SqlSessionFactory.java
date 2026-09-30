package mybatis.session;

import jdbc.template.JdbcTemplate;
import lombok.Getter;
import mybatis.interceptor.Interceptor;
import mybatis.interceptor.InterceptorChain;
import mybatis.mapper.MapperProxy;
import mybatis.mapper.XmlMapperLoader;
import mybatis.mapper.XmlStatement;
import mybatis.resolve.type.TypeHandlerRegistry;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SqlSessionFactory {
    private final JdbcTemplate jdbcTemplate;
    private final List<Interceptor> interceptors;
    private final Map<String, XmlStatement> xmlStatements = new HashMap<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> mapperCaches = new ConcurrentHashMap<>();
    @Getter
    private final TypeHandlerRegistry typeHandlerRegistry;

    public Object getL2(String namespace, String key) {
        ConcurrentHashMap<String, Object> cache = mapperCaches.get(namespace);
        return cache == null ? null : cache.get(key);
    }

    public boolean hasL2(String namespace, String key) {
        ConcurrentHashMap<String, Object> cache = mapperCaches.get(namespace);
        return cache != null && cache.containsKey(key);
    }

    public void putL2(String namespace, String key, Object value) {
        mapperCaches.computeIfAbsent(namespace, _ -> new ConcurrentHashMap<>()).put(key, value);
    }

    public void clearL2(String namespace) {
        ConcurrentHashMap<String, Object> cache = mapperCaches.get(namespace);
        if (cache != null) {
            cache.clear();
        }
    }

    public void loadXml(Class<?> mapperType) {
        for (Map.Entry<String, XmlStatement> stringXmlStatementEntry : XmlMapperLoader.load(mapperType).entrySet()) {
            String key = mapperType.getName() + "." + stringXmlStatementEntry.getKey();
            if (xmlStatements.put(key, stringXmlStatementEntry.getValue()) != null) {
                throw new IllegalStateException("重复 XML 语句" + key);
            }
        }
    }

    public XmlStatement findXml(Class<?> mapperType, String methodName) {
        return xmlStatements.get(mapperType.getName() + "." + methodName);
    }

    public SqlSessionFactory(JdbcTemplate jdbcTemplate, List<Interceptor> interceptors, TypeHandlerRegistry typeHandlerRegistry) {
        this.jdbcTemplate = jdbcTemplate;
        this.interceptors = List.copyOf(interceptors);
        this.typeHandlerRegistry = typeHandlerRegistry;
    }

    public SqlSession openSession() {
        return new SqlSession(jdbcTemplate, new InterceptorChain(interceptors), this);
    }

    public <T> T getMapper(Class<T> type) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                new MapperProxy(this)
        );
    }
}
