package mvc.controller;

import http.base.HttpRequest;
import http.base.HttpResponse;
import mvc.annotation.Controller;
import mvc.annotation.param.PathVariable;
import mvc.annotation.param.ResponseBody;
import mvc.annotation.request.GetMapping;
import mvc.annotation.param.RequestParam;

@Controller("/api")
public class HelloController {

    @GetMapping("/hello/{love}")
    @ResponseBody
    public String hello(@RequestParam("name") String name, @PathVariable("love") String love, HttpRequest request, HttpResponse response) {
        return "Hello world! " + name + " loves " + love;
    }
}
