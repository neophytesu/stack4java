package spring.ioc.bean.lifecycle;

public interface BeanPostProcessor {

    public Object before(Object bean, String beanName);

    public Object after(Object bean, String beanName);
}
