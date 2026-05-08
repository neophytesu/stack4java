package mvc.controller;

import spring.di.annotation.Autowired;
import lombok.Data;
import mvc.annotation.Controller;
import mvc.annotation.param.PathVariable;
import mvc.annotation.param.RequestBody;
import mvc.annotation.param.ResponseBody;
import mvc.annotation.request.GetMapping;
import mvc.annotation.param.RequestParam;
import mvc.annotation.request.PostMapping;
import mvc.view.ModelAndView;
import spring.ioc.bean.lifecycle.destroy.PreDestroy;
import spring.ioc.bean.lifecycle.init.PostConstruct;
import spring.service.interfaces.MyNameService;

import java.util.HashMap;
import java.util.Map;

@Controller("/api")
public class HelloController {

    @Autowired
    MyNameService myNameService;

    @GetMapping("/hello/{love}")
    @ResponseBody
    public String hello(@RequestParam("name") String name, @PathVariable("love") String love) {
        return "Hello world! " + myNameService.getName() + " loves " + love;
    }

    @PostMapping("/bye")
    @ResponseBody
    public String bye(@RequestBody Bye bye) {
        return "GoodBye! " + bye.name;
    }

    @GetMapping("/temp/hello")
    public ModelAndView hello() {
        ModelAndView mav = new ModelAndView();
        Map<String, Object> data = new HashMap<>();
        data.put("name", "yuruyucheng");
        mav.setModel(data);
        mav.setViewName("hello");
        return mav;
    }

    @Data
    public static final class Bye {
        String name;
    }

    @PostConstruct
    public void init(){
        System.out.println("HelloController init");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("HelloController destroy");
    }
}
