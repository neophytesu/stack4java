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

public class SqlSessionFactory {
    private final JdbcTemplate jdbcTemplate;
    private final List<Interceptor> interceptors;
    private final Map<String, XmlStatement> xmlStatements = new HashMap<>();
    @Getter
    private final TypeHandlerRegistry typeHandlerRegistry;
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
