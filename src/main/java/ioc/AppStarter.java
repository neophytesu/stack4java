package ioc;

import http.HttpServer;
import ioc.bean.AppConfig;
import ioc.bean.DefaultBeanFactory;
import mvc.DispatcherServlet;

public class AppStarter {
    static void main(String[] args) throws Exception {
        AppConfig config = new AppConfig();
        DefaultBeanFactory factory = new DefaultBeanFactory();
        for (Class<?> clazz : config.controllerClasses()) {
            factory.register(clazz);
        }
        DispatcherServlet dispatcherServlet = new DispatcherServlet(factory, config);
        HttpServer server = new HttpServer(8080);
        server.addServlet("/api/*", dispatcherServlet, "dispatcher");
        server.start();
    }
}
