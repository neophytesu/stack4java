package mybatis;

import mybatis.annotation.Mapper;
import spring.core.DefaultBeanFactory;
import spring.ioc.bean.ClassScanner;

public class MapperScanner {
    private final DefaultBeanFactory factory;

    public MapperScanner(DefaultBeanFactory factory) {
        this.factory = factory;
    }

    public void scan(String basePackage) throws Exception {
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) factory.getBean(SqlSessionFactory.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        ClassScanner scanner = new ClassScanner();
        for (Class<?> clazz : scanner.loadPackage(basePackage)) {
            if (!clazz.isInterface()) {
                continue;
            }
            if (!clazz.isAnnotationPresent(Mapper.class)) {
                continue;
            }
            factory.register(clazz, sqlSession.getMapper(clazz));
        }
    }
}
