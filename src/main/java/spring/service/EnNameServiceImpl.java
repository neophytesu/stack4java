package spring.service;

import spring.ioc.annotation.Qualifier;
import spring.service.annotations.Service;
import spring.service.interfaces.MyNameService;

@Qualifier("su")
@Service
public class EnNameServiceImpl implements MyNameService {
    @Override
    public String getName() {
        return "su";
    }
}
