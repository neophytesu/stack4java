package mybatis.interceptor;

import jdbc.RowMapper;
import mybatis.session.SqlSession;

import java.util.List;

public record Invocation(SqlSession target, String name, Object[] args, List<Interceptor> interceptors, int index) {
    public Object proceed() throws Exception {
        if (index < interceptors.size()) {
            return interceptors.get(index).intercept(new Invocation(target, name, args, interceptors, index + 1));
        }
        return switch (name) {
            case "selectOne" ->
                    target.doSelectOne((String) args[0], (Object[]) args[1], (RowMapper<?>) args[2], (String) args[3], (Boolean) args[4]);
            case "selectList" ->
                    target.doSelectList((String) args[0], (Object[]) args[1], (RowMapper<?>) args[2], (String) args[3], (Boolean) args[4]);
            case "update" -> target.doUpdate((String) args[0], (Object[]) args[1], (String) args[2], (Boolean) args[3]);
            case "updateAndReturnKey" ->
                    target.doUpdateAndReturnKey((String) args[0], (Object[]) args[1], (String) args[2], (Boolean) args[3]);
            default -> throw new IllegalStateException(name);
        };
    }
}
