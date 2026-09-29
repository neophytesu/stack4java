package mybatis;

import jdbc.ResultSet;
import jdbc.RowMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;

public class PojoRowMapper<T> implements RowMapper<T> {
    private final Constructor<T> constructor;
    private final RecordComponent[] components;

    public PojoRowMapper(Class<T> type) {
        if (!type.isRecord()) {
            throw new IllegalStateException("这一版只映射 record：" + type);
        }
        this.components = type.getRecordComponents();
        this.constructor = (Constructor<T>) type.getDeclaredConstructors()[0];
        this.constructor.setAccessible(true);
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws Exception{
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            args[i] = rs.getObject(components[i].getName());
        }
        return constructor.newInstance(args);
    }
}
