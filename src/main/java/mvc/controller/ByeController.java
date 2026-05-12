package mvc.controller;

import mvc.annotation.param.ResponseBody;
import mvc.annotation.request.GetMapping;
import spring.di.annotation.Autowired;
import mvc.annotation.Controller;
import spring.ioc.bean.lifecycle.destroy.PreDestroy;
import spring.ioc.bean.lifecycle.init.PostConstruct;
import spring.service.CodeNameServiceImpl;

@Controller("/api")
public class ByeController {

    @Autowired
    CodeNameServiceImpl codeNameService;

    @Autowired
    HelloController helloController;

    @GetMapping("/bye")
    @ResponseBody
    public String bye() {
        return "GoodBye! " + codeNameService.getCodeName();
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
