package mybatis.resolve.type;

import jdbc.PreparedStatement;
import jdbc.ResultSet;

public class EnumTypeHandler<E extends Enum<E>> implements TypeHandler<E> {
    private final Class<E> type;

    public EnumTypeHandler(Class<E> type) {
        this.type = type;
    }

    @Override
    public void setParameter(PreparedStatement ps, int index, E value) {
        ps.setString(index, value.name());
    }

    @Override
    public E getResult(ResultSet rs, String column) {
        Object raw = rs.getObject(column);
        if (raw == null) {
            return null;
        }
        return Enum.valueOf(type, raw.toString());
    }
}
