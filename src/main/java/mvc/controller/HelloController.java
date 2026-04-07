package mvc.controller;

import http.base.HttpRequest;
import http.base.HttpResponse;
import lombok.Data;
import mvc.annotation.Controller;
import mvc.annotation.param.PathVariable;
import mvc.annotation.param.RequestBody;
import mvc.annotation.param.ResponseBody;
import mvc.annotation.request.GetMapping;
import mvc.annotation.param.RequestParam;
import mvc.annotation.request.PostMapping;

@Controller("/api")
public class HelloController {

    @GetMapping("/hello/{love}")
    @ResponseBody
    public String hello(@RequestParam("name") String name, @PathVariable("love") String love, HttpRequest request, HttpResponse response) {
        return "Hello world! " + name + " loves " + love;
    }

    @PostMapping("/bye")
    @ResponseBody
    public String bye(@RequestBody Bye bye) {
        return "GoodBye! " + bye.name;
    }
    @Data
    public static final class Bye {
        String name;
    }
}
