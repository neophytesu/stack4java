package spring.ioc.bean;

import jdbc.DataSource;
import jdbc.support.PooledDataSource;
import jdbc.template.JdbcTemplate;
import mvc.view.PrefixSuffixViewResolver;
import mybatis.interceptor.LogInterceptor;
import mybatis.resolve.type.TypeHandlerRegistry;
import mybatis.session.SqlSessionFactory;
import mysql.config.SqlEngineBootstrap;
import spring.ioc.annotation.Bean;
import spring.ioc.annotation.Configuration;

import java.util.List;

@Configuration
public class InfraConfig {
    @Bean
    DataSource dataSource() {
        return new PooledDataSource(SqlEngineBootstrap.createCatalogAndInit(), 4, "app");
    }

    @Bean
    TypeHandlerRegistry typeHandlerRegistry() {
        return new TypeHandlerRegistry();
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource, TypeHandlerRegistry registry) {
        return new JdbcTemplate(dataSource, registry);
    }

    @Bean
    SqlSessionFactory sqlSessionFactory(JdbcTemplate jdbcTemplate, TypeHandlerRegistry registry) {
        return new SqlSessionFactory(jdbcTemplate, List.of(new LogInterceptor()), registry);
    }

    @Bean
    PrefixSuffixViewResolver viewResolver() {
        return new PrefixSuffixViewResolver();
    }
}
