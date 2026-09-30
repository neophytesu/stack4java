package mybatis.resolve.type;

import jdbc.PreparedStatement;
import jdbc.ResultSet;

public class ObjectTypeHandler implements TypeHandler<Object> {
    @Override
    public void setParameter(PreparedStatement ps, int index, Object value) {
        ps.setObject(index, value);
    }

    @Override
    public Object getResult(ResultSet rs, String column) {
        return rs.getObject(column);
    }
}
