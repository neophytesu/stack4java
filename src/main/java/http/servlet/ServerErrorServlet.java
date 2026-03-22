package http.servlet;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.nio.charset.StandardCharsets;

public class ServerErrorServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        response.setVersion("HTTP/1.1");
        response.setStatusCode(500);
        response.setStatusDesc("Internal Server Error");
        response.setBody("Internal Server Error!".getBytes(StandardCharsets.UTF_8));
        response.getHeaders().put("Content-Type", "text/plain;charset=utf-8");
        response.getHeaders().put("Content-Length", String.valueOf(response.getBody().length));
    }
}
