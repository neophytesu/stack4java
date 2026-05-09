package spring.core;

@FunctionalInterface
public interface ObjectFactory<T> {
    T getObject() throws Exception;
}
