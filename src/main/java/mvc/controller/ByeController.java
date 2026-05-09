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
public class ByeController {

    @Autowired
    MyNameService myNameService;

    @Autowired
    HelloController helloController;

    @PostMapping("/bye")
    @ResponseBody
    public String bye(@RequestBody Bye bye) {
        return "GoodBye! " + bye.name;
    }

    @Data
    public static final class Bye {
        String name;
    }

    @PostConstruct
    public void init() {
        System.out.println("ByeController init");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("ByeController destroy");
    }
}
