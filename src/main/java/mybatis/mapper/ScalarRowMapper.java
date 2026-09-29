package mybatis.mapper;

import jdbc.ResultSet;
import jdbc.RowMapper;

public class ScalarRowMapper<T> implements RowMapper<T> {
    private final Class<T> type;

    public ScalarRowMapper(Class<T> type) {
        this.type = type;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) {
        return convert(rs.getObject(1), type);
    }

    private static <T> T convert(Object value, Class<T> type) {
        if (value == null) {
            if (type.isPrimitive()) {
                throw new IllegalStateException("基本类型不能是 null：" + type);
            }
            return null;
        }
        if (type.isInstance(value) || wrap(type).isInstance(value)) {
            return (T) value;
        }
        if (value instanceof Number n) {
            if (type == int.class || type == Integer.class) {
                return (T) Integer.valueOf(n.intValue());
            }
            if (type == long.class || type == Long.class) {
                return (T) Long.valueOf(n.longValue());
            }
        }
        if (type == String.class) {
            return (T) String.valueOf(value);
        }
        throw new IllegalStateException("无法把 " + value.getClass() + " 转成 " + type);
    }

    private static Class<?> wrap(Class<?> type) {
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        return type;
    }
}
