package http.servlet;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.nio.charset.StandardCharsets;

public class NotFoundServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        response.setStatusCode(404);
        response.setStatusDesc("Not Found");
        response.setBody("Not Found Page!".getBytes(StandardCharsets.UTF_8));
        response.getHeaders().put("Content-Type", "text/html;charset=utf-8");
        response.getHeaders().put("Content-Length", String.valueOf(response.getBody().length));
    }
}
