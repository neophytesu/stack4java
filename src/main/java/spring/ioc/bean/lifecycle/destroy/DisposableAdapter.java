package spring.ioc.bean.lifecycle.destroy;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

@Data
@AllArgsConstructor
public class DisposableAdapter {
    private String beanName;
    private Object bean;
    private List<Method> preDestroyMethods;
    private boolean disposableBean;

    public void destroy() throws Exception {
        for (Method method : preDestroyMethods) {
            method.setAccessible(true);
            method.invoke(bean);
        }
        if (disposableBean) {
            ((DisposableBean) bean).destroy();
        }
    }
}
