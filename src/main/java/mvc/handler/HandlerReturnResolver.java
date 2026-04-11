package mvc.handler;

import http.base.HttpRequest;
import http.base.HttpResponse;
import lombok.Setter;
import mvc.annotation.param.ResponseBody;
import mvc.view.ModelAndView;
import mvc.view.interfaces.View;
import mvc.view.interfaces.ViewResolver;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HandlerReturnResolver {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Setter
    private ViewResolver viewResolver;

    void resolve(Object result, Method method, HttpRequest request, HttpResponse response) throws IOException {
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(void.class)) {
            return;
        }
        boolean hasResponseBody = method.isAnnotationPresent(ResponseBody.class) || method.getDeclaringClass().isAnnotationPresent(ResponseBody.class);
        if (result instanceof ModelAndView mav) {
            View view = viewResolver.resolveViewName(mav.getViewName());
            if (view == null) {
                throw new IllegalStateException("视图" + mav.getViewName() + "不存在！");
            }
            view.render(mav.getModel() == null ? Map.of() : mav.getModel(), request, response);
            return;
        }
        if (result instanceof String s) {
            if (hasResponseBody) {
                buildResponse(response, s, "text/plain;charset=utf-8");
            } else {
                View view = viewResolver.resolveViewName(s);
                if (view == null) {
                    throw new IllegalStateException("视图" + s + "不存在！");
                }
                view.render(Map.of(), request, response);
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
