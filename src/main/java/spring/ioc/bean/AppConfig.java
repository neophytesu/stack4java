package spring.ioc.bean;

import java.util.ArrayList;
import java.util.List;

public class AppConfig {
    public Class<?>[] controllerClasses() throws Exception {
        ClassScanner sc = new ClassScanner();
        List<Class<?>> all = new ArrayList<>();
        all.addAll(sc.scanPackage("mvc.controller"));
        all.addAll(sc.scanPackage("mvc.view"));
        all.addAll(sc.scanPackage("spring.service"));
        return all.toArray(Class[]::new);
    }
}
