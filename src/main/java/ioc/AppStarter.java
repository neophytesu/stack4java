package ioc;

import http.HttpServer;
import http.base.HttpRequest;
import http.base.HttpResponse;
import http.servlet.SuccessServlet;
import mvc.DispatcherServlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
