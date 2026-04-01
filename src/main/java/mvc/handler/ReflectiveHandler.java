package mvc.handler;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.lang.reflect.Method;

public class ReflectiveHandler implements Handler {

    private final Object controller;
    private final Method method;

    private final HandlerMethodInvoker handlerMethodInvoker = new HandlerMethodInvoker();
    private final HandlerReturnResolver handlerReturnResolver = new HandlerReturnResolver();

    public ReflectiveHandler(Object controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Throwable {
        Object result = handlerMethodInvoker.invoke(controller, method, request, response);
        handlerReturnResolver.resolve(result, method, response);
    }
}
