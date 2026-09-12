package spring;

import http.HttpServer;
import jdbc.DataSource;
import jdbc.support.PooledDataSource;
import mysql.config.SqlEngineBootstrap;
import spring.aop.advisor.SimpleAdvisor;
import spring.aop.interceptor.LogMethodInterceptor;
import spring.aop.interceptor.TransactionalInterceptor;
import spring.aop.pointcut.LogMethodPointcut;
import spring.aop.pointcut.TransactionalPointcut;
import spring.ioc.bean.AppConfig;
import spring.core.DefaultBeanFactory;
import mvc.DispatcherServlet;
import spring.ioc.bean.lifecycle.BeanPostProcessor;

import java.util.List;

public class AppStarter {
    static void main() throws Exception {
        AppConfig config = new AppConfig();
        DefaultBeanFactory factory = new DefaultBeanFactory();
        factory.addBeanPostProcessor(new BeanPostProcessor() {
            @Override
            public Object before(Object bean, String beanName) {
                System.out.println(beanName + "初始化前");
                return bean;
            }

            @Override
            public Object after(Object bean, String beanName) {
                System.out.println(beanName + "初始化后");
                return bean;
            }
        });
        factory.register(new PooledDataSource(SqlEngineBootstrap.createCatalogAndInit(), 4, "app"));
        for (Class<?> clazz : config.controllerClasses()) {
            factory.register(clazz);
        }
        factory.addAdvisors(List.of(
                new SimpleAdvisor(new LogMethodPointcut(), new LogMethodInterceptor()),
                new SimpleAdvisor(new TransactionalPointcut(), new TransactionalInterceptor((DataSource) factory.getBean(DataSource.class)))));
        DispatcherServlet dispatcherServlet = new DispatcherServlet(factory, config);
        HttpServer server = new HttpServer(8080);
        server.addServlet("/api/*", dispatcherServlet, "dispatcher");
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "httpServer");
        serverThread.setDaemon(false);
        serverThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
                serverThread.join(60_000);
                factory.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
        serverThread.join();
    }
}
