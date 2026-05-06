package spring.ioc.bean.lifecycle.aware;

import spring.core.DefaultBeanFactory;

public interface BeanFactoryAware {
    public void setBeanFactory(DefaultBeanFactory factory);
}
