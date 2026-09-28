package mybatis;

import jdbc.template.JdbcTemplate;
import mybatis.annotation.Mapper;
import spring.core.DefaultBeanFactory;
import spring.ioc.bean.ClassScanner;

import java.lang.reflect.Proxy;

public class MapperScanner {
    private final DefaultBeanFactory factory;

    public MapperScanner(DefaultBeanFactory factory) {
        this.factory = factory;
    }

    public void scan(String basePackage) throws Exception {
        JdbcTemplate jdbcTemplate = (JdbcTemplate) factory.getBean(JdbcTemplate.class);
        ClassScanner scanner = new ClassScanner();
        for (Class<?> clazz : scanner.loadPackage(basePackage)) {
            if (!clazz.isInterface()) {
                continue;
            }
            if (!clazz.isAnnotationPresent(Mapper.class)) {
                continue;
            }
            Object proxy = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new MapperProxy(jdbcTemplate));
            factory.register(clazz, proxy);
        }
    }
}
