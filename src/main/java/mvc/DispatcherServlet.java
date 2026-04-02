package mvc;

import http.HttpHelper;
import http.HttpServletConfig;
import http.base.HttpRequest;
import http.base.HttpResponse;
import http.servlet.HttpServlet;
import lombok.Getter;
import mvc.annotation.Controller;
import mvc.annotation.request.GetMapping;
import mvc.annotation.request.RequestMapping;
import mvc.controller.HelloController;
import mvc.handler.Handler;
import mvc.handler.ReflectiveHandler;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DispatcherServlet implements HttpServlet {

    private final HttpHelper httpHelper = new HttpHelper();

    @Getter
    private HttpServletConfig servletConfig;

    private final Map<String, Handler> handlerMap = new HashMap<>();

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
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
        } catch (Throwable e) {
            Throwable t = e;
            if (e instanceof InvocationTargetException) {
                t = ((InvocationTargetException) e).getTargetException();
                if (t == null) {
                    t = e;
                }
            }
            request.getAttributes().put("exception", t);
            request.getRequestDispatcher("/error").forward(request, response);
        }
    }

    @Override
    public void init(HttpServletConfig config) {
        HttpServlet.super.init(config);
        this.servletConfig = config;
        try {
            registerController(new HelloController());
        } catch (Exception e) {
            System.out.println("Controller Register Failed!");
            System.out.println(e.getCause().getMessage());
        }
    }

    private void registerController(Object controller) throws Exception {
        Class<?> clazz = controller.getClass();
        String basePath = "";
        if (clazz.isAnnotationPresent(Controller.class)) {
            basePath = clazz.getAnnotation(Controller.class).value();
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            Annotation ann = Arrays.stream(method.getAnnotations())
                    .filter(annotation -> annotation.annotationType().isAnnotationPresent(RequestMapping.class))
                    .findFirst().orElse(null);
            if (ann == null) {
                continue;
            }
            String meta = ann.annotationType().getAnnotation(RequestMapping.class).value();
            String subPath = ann.annotationType().getMethod("value").invoke(ann).toString();
            String fullPath = normalize(basePath) + normalize(subPath);
            String key = meta + " " + fullPath;
            if (handlerMap.containsKey(key)) {
                throw new RuntimeException("Thera are two method use same path!");
            }
            handlerMap.put(key, new ReflectiveHandler(controller, method));
        }
    }

    private String normalize(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public void destroy() {
        HttpServlet.super.destroy();
    }
}
