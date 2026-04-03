package mvc.controller;

import http.base.HttpRequest;
import http.base.HttpResponse;
import mvc.annotation.Controller;
import mvc.annotation.ResponseBody;
import mvc.annotation.request.GetMapping;
import mvc.annotation.RequestParam;

import java.nio.charset.StandardCharsets;

@Controller("/api")
public class HelloController {

    @GetMapping("/hello")
    @ResponseBody
    public String hello(@RequestParam("name") String name, HttpRequest request, HttpResponse response) {
        return "Hello world! " + name;
    }
}
