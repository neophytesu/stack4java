package mybatis.resolve.type;

import java.util.HashMap;
import java.util.Map;

public class TypeHandlerRegistry {
    private final TypeHandler<Object> objectTypeHandler = new ObjectTypeHandler();
    private final Map<Class<?>, TypeHandler<?>> handlerMap = new HashMap<>();

    public TypeHandler<?> get(Class<?> type) {
        TypeHandler<?> typeHandler = handlerMap.get(type);
        if (typeHandler != null) {
            return typeHandler;
        }
        if (type.isEnum()) {
            EnumTypeHandler<?> created = new EnumTypeHandler<>((Class) type);
            handlerMap.put(type, created);
            return created;
        }
        return objectTypeHandler;
    }
}
