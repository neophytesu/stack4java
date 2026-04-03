package mvc.handler;

import http.base.HttpResponse;
import mvc.annotation.ResponseBody;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HandlerReturnResolver {

    private final ObjectMapper objectMapper = new ObjectMapper();

    void resolve(Object result, Method method, HttpResponse response) {
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(void.class)) {
            return;
        }
        boolean hasResponseBody = method.isAnnotationPresent(ResponseBody.class) || method.getDeclaringClass().isAnnotationPresent(ResponseBody.class);
        if (result instanceof String s) {
            if (hasResponseBody) {
                buildResponse(response, s, "text/plain;charset=utf-8");
            }
            return;
        }
        if (result == null) {
            response.setStatusDesc("No Content");
            response.setStatusCode(204);
            return;
        }
        if (hasResponseBody) {
            String json = objectMapper.writeValueAsString(result);
            buildResponse(response, json, "application/json;charset=utf-8");
        }
    }

    private void buildResponse(HttpResponse response, String data, String contentType) {
        response.setBody(data.getBytes(StandardCharsets.UTF_8));
        response.setStatusDesc("OK");
        response.setStatusCode(200);
        Map<String, String> headers = response.getHeaders();
        headers.put("Content-Type", contentType);
        headers.put("Content-Length", String.valueOf(data.getBytes(StandardCharsets.UTF_8).length));
        response.setHeaders(headers);
    }

}
