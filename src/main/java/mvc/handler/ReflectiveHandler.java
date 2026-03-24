package mvc.handler;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ReflectiveHandler implements Handler {

    private final Object controller;
    private final Method method;

    public ReflectiveHandler(Object controller, Method method) {
        this.controller = controller;
        this.method = method;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        Class<?>[] parameterTypes = method.getParameterTypes();
        int n = parameterTypes.length;
        Object result = null;
        if (n == 0) {
            result = method.invoke(controller);
        } else if (n == 2 && parameterTypes[0] == HttpRequest.class && parameterTypes[1] == HttpResponse.class) {
            result = method.invoke(controller, request, response);
        } else {
            throw new IllegalArgumentException("暂不支持的签名");
        }
        if (result instanceof String s) {
            response.setBody(s.getBytes(StandardCharsets.UTF_8));
            response.setStatusDesc("OK");
            response.setStatusCode(200);
            Map<String, String> headers = response.getHeaders();
            headers.put("Content-Type", "text/plain;charset=utf-8");
            headers.put("Content-Length", String.valueOf(s.getBytes(StandardCharsets.UTF_8).length));
            response.setHeaders(headers);
        }
    }
}
