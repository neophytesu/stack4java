package mvc;

import http.HttpHelper;
import http.HttpServletConfig;
import http.base.HttpRequest;
import http.base.HttpResponse;
import http.servlet.HttpServlet;
import mvc.handler.GlobalExceptionHandler;
import spring.ioc.bean.AppConfig;
import spring.core.DefaultBeanFactory;
import lombok.Getter;
import mvc.annotation.Controller;
import mvc.annotation.request.RequestMapping;
import mvc.common.route.SegType;
import mvc.common.route.Segment;
import mvc.common.route.RouteEntry;
import mvc.handler.Handler;
import mvc.handler.ReflectiveHandler;
import mvc.view.PrefixSuffixViewResolver;
import mvc.view.interfaces.ViewResolver;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DispatcherServlet implements HttpServlet {

    private final HttpHelper httpHelper = new HttpHelper();

    @Getter
    private HttpServletConfig servletConfig;

    private final Map<String, Handler> handlerMap = new HashMap<>();

    private final List<RouteEntry> routeEntryList = new ArrayList<>();

    private final DefaultBeanFactory beanFactory;
    private final AppConfig appConfig;

    private ViewResolver viewResolver;

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    public DispatcherServlet(DefaultBeanFactory beanFactory, AppConfig appConfig) {
        this.beanFactory = beanFactory;
        this.appConfig = appConfig;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String method = request.getMethod();
        String path = httpHelper.normalizePath(request.getPath());
        String key = method + " " + path;
        Handler handler = null;
        if (handlerMap.containsKey(key)) {
            request.getAttributes().remove("pathVariables");
            handler = handlerMap.get(key);
        } else {
            for (RouteEntry routeEntry : routeEntryList) {
                if (!routeEntry.getMethod().equals(method)) {
                    continue;
                }
                Optional<Map<String, String>> vars = routeEntry.tryMatch(path);
                if (vars.isPresent()) {
                    request.getAttributes().put("pathVariables", vars.get());
                    handler = routeEntry.getHandler();
                    break;
                }
            }
        }
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
            globalExceptionHandler.resolve(unwrapInvocationTarget(e), request, response);
        }
    }

    private Throwable unwrapInvocationTarget(Throwable e) {
        Throwable t = e;
        if (e instanceof InvocationTargetException) {
            t = ((InvocationTargetException) e).getTargetException();
            if (t == null) {
                return e;
            }
        }
        return t;
    }

    @Override
    public void init(HttpServletConfig config) {
        HttpServlet.super.init(config);
        this.servletConfig = config;
        viewResolver = (ViewResolver) beanFactory.getBean(PrefixSuffixViewResolver.class);
        try {
            for (Class<?> clazz : this.appConfig.controllerClasses()) {
                registerController(beanFactory.getBean(clazz));
            }
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
            if (fullPath.contains("{") && fullPath.contains("}")) {
                RouteEntry routeEntry = new RouteEntry();
                routeEntry.setMethod(meta);
                routeEntry.setHandler(new ReflectiveHandler(controller, method, viewResolver));
                List<Segment> patternSegments = new ArrayList<>();
                for (String s : fullPath.split("/")) {
                    if (s.isBlank()) {
                        continue;
                    }
                    Segment segment = new Segment();
                    if (s.startsWith("{") && s.endsWith("}")) {
                        s = s.substring(1, s.length() - 1);
                        segment.setName(s);
                        segment.setType(SegType.VARIABLE);
                    } else {
                        segment.setName(s);
                        segment.setType(SegType.LITERAL);
                    }
                    patternSegments.add(segment);
                }
                routeEntry.setPatternSegments(patternSegments);
                routeEntryList.add(routeEntry);
            } else {
                String key = meta + " " + fullPath;
                if (handlerMap.containsKey(key)) {
                    throw new RuntimeException("There are two method use same path!");
                }
                handlerMap.put(key, new ReflectiveHandler(controller, method, viewResolver));
            }
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
