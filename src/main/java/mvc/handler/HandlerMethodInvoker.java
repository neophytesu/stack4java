package mvc.handler;

import http.base.HttpRequest;
import http.base.HttpResponse;
import mvc.annotation.param.PathVariable;
import mvc.annotation.param.RequestParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class HandlerMethodInvoker {
    Object invoke(Object controller, Method method, HttpRequest request, HttpResponse response) throws Throwable {
        Class<?>[] types = method.getParameterTypes();
        Parameter[] params = method.getParameters();
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            args[i] = resolveArgument(types[i], params[i], request, response);
        }
        Object result;
        try {
            result = method.invoke(controller, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }

    private Object resolveArgument(Class<?> type, Parameter param, HttpRequest request, HttpResponse response) {
        if (type.equals(HttpResponse.class)) {
            return response;
        }
        if (type.equals(HttpRequest.class)) {
            return request;
        }
        if (param.isAnnotationPresent(PathVariable.class)
                && request.getAttributes().get("pathVariables") instanceof Map<?, ?> pathVariables) {
            return pathVariables.get(param.getAnnotation(PathVariable.class).value());
        }
        if (type.equals(String.class) && param.isAnnotationPresent(RequestParam.class)) {
            return request.getParamValue(param.getAnnotation(RequestParam.class).value());
        }
        throw new RuntimeException("unsupported argument type");
    }
}
