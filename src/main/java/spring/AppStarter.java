package spring;

import http.HttpServer;
import spring.aop.interceptor.LogMethodInterceptor;
import spring.ioc.bean.AppConfig;
import spring.core.DefaultBeanFactory;
import mvc.DispatcherServlet;
import spring.ioc.bean.lifecycle.BeanPostProcessor;

import java.util.List;

public class AppStarter {
    static void main(String[] args) throws Exception {
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
        factory.addInterceptors(List.of(new LogMethodInterceptor()));
        for (Class<?> clazz : config.controllerClasses()) {
            factory.register(clazz);
        }
        DispatcherServlet dispatcherServlet = new DispatcherServlet(factory, config);
        HttpServer server = new HttpServer(8080);
        server.addServlet("/api/*", dispatcherServlet, "dispatcher");
        server.start();
    }
}
