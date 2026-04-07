package mvc.handler;

import http.base.HttpRequest;
import http.base.HttpResponse;
import mvc.annotation.param.PathVariable;
import mvc.annotation.param.RequestBody;
import mvc.annotation.param.RequestParam;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HandlerMethodInvoker {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        if (param.isAnnotationPresent(RequestBody.class)) {
            String json = request.getBodyJson();
            if (json == null && request.getBody() != null) {
                json = new String(request.getBody(), StandardCharsets.UTF_8);
            }
            boolean required = param.getAnnotation(RequestBody.class).required();
            if ((json == null || json.isBlank()) && required) {
                throw new RuntimeException("request's json body is blank");
            }
            if ((json == null || json.isBlank()) && !required) {
                if (param.getType().isPrimitive()) {
                    return getPrimitiveDefaultValue(param.getType());
                }
                return null;
            }
            Type t = param.getParameterizedType();
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructType(t));
        }
        throw new RuntimeException("unsupported argument type");
    }

    private Object getPrimitiveDefaultValue(Class<?> type) {
        return Array.get(Array.newInstance(type, 1), 0);
    }
}
