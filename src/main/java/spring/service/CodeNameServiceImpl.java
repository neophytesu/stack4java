package spring.service;

import spring.service.annotations.LogMethod;
import spring.service.annotations.Service;

@LogMethod
@Service
public class CodeNameServiceImpl {
    public String getCodeName() {
        return "玉";
    }
}
