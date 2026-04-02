package mvc.controller;

import http.base.HttpRequest;
import http.base.HttpResponse;
import mvc.annotation.Controller;
import mvc.annotation.request.GetMapping;
import mvc.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
@Controller("/api")
public class HelloController {

    @GetMapping("/hello")
    public void hello(@RequestParam("name") String name, HttpRequest request, HttpResponse response) {
        response.setVersion("HTTP/1.1");
        response.setStatusCode(200);
        response.setStatusDesc("Hello World");
        response.setBody(("Hello World! " + name).getBytes(StandardCharsets.UTF_8));
        response.getHeaders().put("Content-Type", "text/plain;charset=utf-8");
        response.getHeaders().put("Content-Length", String.valueOf(response.getBody().length));
    }
}
