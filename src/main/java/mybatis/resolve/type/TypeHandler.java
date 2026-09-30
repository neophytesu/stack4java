package mybatis.resolve.type;

import jdbc.PreparedStatement;
import jdbc.ResultSet;

public interface TypeHandler<T> {
    void setParameter(PreparedStatement ps, int index, T value);

    T getResult(ResultSet rs, String column);
}
