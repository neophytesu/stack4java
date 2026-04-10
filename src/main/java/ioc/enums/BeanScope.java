package ioc.enums;

import lombok.Getter;

public enum BeanScope {
    SINGLETON("singleton"),
    PROTOTYPE("prototype");
    @Getter
    private final String scope;

    BeanScope(String scope) {
        this.scope = scope;
    }
}
