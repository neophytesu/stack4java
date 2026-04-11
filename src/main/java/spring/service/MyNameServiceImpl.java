package spring.service;

import spring.service.annotations.LogMethod;
import spring.service.annotations.Service;
import spring.service.interfaces.MyNameService;

@LogMethod
@Service
public class MyNameServiceImpl implements MyNameService {
    @Override
    public String getName() {
        return "hyc";
    }
}
