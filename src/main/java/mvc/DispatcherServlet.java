package mvc;

import http.HttpHelper;
import http.HttpServletConfig;
import http.base.HttpRequest;
import http.base.HttpResponse;
import http.servlet.HttpServlet;
import lombok.Getter;
import mvc.controller.HelloController;
import mvc.handler.Handler;
import mvc.handler.ReflectiveHandler;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DispatcherServlet implements HttpServlet {

    private final HttpHelper httpHelper = new HttpHelper();

    @Getter
    private HttpServletConfig servletConfig;

    private final Map<String, Handler> handlerMap = new HashMap<>();

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        String method = request.getMethod();
        String path = httpHelper.normalizePath(request.getPath());
        String key = method + " " + path;
        Handler handler = handlerMap.get(key);
        if (handler == null) {
            response.setStatusCode(404);
            response.setStatusDesc("Not Found");
            response.setBody("Not Found".getBytes(StandardCharsets.UTF_8));
            response.getHeaders().put("Content-Type", "text/plain;charset=utf-8");
            response.getHeaders().put("Content-Length", String.valueOf(response.getBody().length));
            return;
        }
        try {
            handler.handle(request, response);
        }  catch (Exception e) {
            Throwable t=e;
            if (e instanceof InvocationTargetException) {
                t=((InvocationTargetException)e).getTargetException();
                if (t==null) {
                    t=e;
                }
            }
            t.printStackTrace();
            response.setStatusCode(500);
            response.setStatusDesc("Internal Server Error");
            response.setBody("Internal Server Error".getBytes(StandardCharsets.UTF_8));
            response.getHeaders().put("Content-Type", "text/plain;charset=utf-8");
            response.getHeaders().put("Content-Length", String.valueOf(response.getBody().length));
            System.out.println(e.getCause().getMessage());
        }
    }

    @Override
    public void init(HttpServletConfig config) {
        HttpServlet.super.init(config);
        this.servletConfig = config;
        try {
            registerRoutes();
        } catch (Exception e) {
            System.out.println("Controller Register Failed!");
            System.out.println(e.getCause().getMessage());
        }
    }

    private void registerRoutes() throws NoSuchMethodException {
        HelloController helloController = new HelloController();
        handlerMap.put("GET /api/hello", new ReflectiveHandler(helloController, HelloController.class.getMethod("hello", HttpRequest.class, HttpResponse.class)));
    }

    @Override
    public void destroy() {
        HttpServlet.super.destroy();
    }
}
