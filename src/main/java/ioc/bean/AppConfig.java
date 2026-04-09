package ioc.bean;

public class AppConfig {
    public Class<?>[] controllerClasses() throws Exception {
        ClassScanner sc = new ClassScanner();
        return sc.scanPackage("mvc.controller").toArray(Class<?>[]::new);
    }
}
