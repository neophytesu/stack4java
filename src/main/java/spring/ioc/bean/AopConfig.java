package spring.ioc.bean;

import jdbc.DataSource;
import mybatis.session.SqlSessionFactory;
import spring.aop.advisor.Advisor;
import spring.aop.advisor.SimpleAdvisor;
import spring.aop.interceptor.LogMethodInterceptor;
import spring.aop.interceptor.TransactionalInterceptor;
import spring.aop.pointcut.LogMethodPointcut;
import spring.aop.pointcut.TransactionalPointcut;
import spring.ioc.annotation.Bean;
import spring.ioc.annotation.Configuration;

@Configuration
public class AopConfig {
    @Bean
    LogMethodInterceptor logMethodInterceptor() {
        return new LogMethodInterceptor();
    }

    @Bean
    Advisor logAdvisor(LogMethodInterceptor logMethodInterceptor) {
        return new SimpleAdvisor(new LogMethodPointcut(), logMethodInterceptor);
    }

    @Bean
    TransactionalInterceptor transactionalInterceptor(DataSource dataSource, SqlSessionFactory sqlSessionFactory) {
        return new TransactionalInterceptor(dataSource, sqlSessionFactory);
    }

    @Bean
    Advisor txAdvisor(TransactionalInterceptor transactionalInterceptor) {
        return new SimpleAdvisor(new TransactionalPointcut(), transactionalInterceptor);
    }
}
