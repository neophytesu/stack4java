package mybatis.mapper;

import jdbc.ResultSet;
import jdbc.RowMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Map;

public class PojoRowMapper<T> implements RowMapper<T> {
    private final Constructor<T> constructor;
    private final RecordComponent[] components;
    private final Map<String, String> columnByProperty;

    public PojoRowMapper(Class<T> type, Map<String, String> columnByProperty) {
        if (!type.isRecord()) {
            throw new IllegalStateException("这一版只映射 record：" + type);
        }
        this.components = type.getRecordComponents();
        this.constructor = (Constructor<T>) type.getDeclaredConstructors()[0];
        this.constructor.setAccessible(true);
        this.columnByProperty = columnByProperty;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws Exception {
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            String property = components[i].getName();
            String column = columnByProperty.getOrDefault(property, property);
            args[i] = rs.getObject(column);
        }
        return constructor.newInstance(args);
    }
}
