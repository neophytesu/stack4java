package http.servlet;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ServerErrorServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        response.setVersion("HTTP/1.1");
        response.setStatusCode(500);
        response.setStatusDesc("Internal Server Error");
        Object exception = request.getAttributes().get("exception");
        if (!Objects.isNull(exception) && exception instanceof Throwable t) {
            String mes = t.getMessage();
            if (mes == null) {
                mes = "Unknown error";
            }
            response.setBody(mes.getBytes(StandardCharsets.UTF_8));
        } else {
            response.setBody("Internal Server Error!".getBytes(StandardCharsets.UTF_8));
        }
        response.getHeaders().put("Content-Type", "text/plain;charset=utf-8");
        response.getHeaders().put("Content-Length", String.valueOf(response.getBody().length));
    }
}
