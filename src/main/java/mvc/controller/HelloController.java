package mvc.controller;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.nio.charset.StandardCharsets;

public class HelloController {

    public void hello(HttpRequest request, HttpResponse response) {
        response.setVersion("HTTP/1.1");
        response.setStatusCode(200);
        response.setStatusDesc("Hello World");
        response.setBody("Hello World".getBytes(StandardCharsets.UTF_8));
        response.getHeaders().put("Content-Type", "text/plain;charset=utf-8");
        response.getHeaders().put("Content-Length", String.valueOf(response.getBody().length));
    }
}
