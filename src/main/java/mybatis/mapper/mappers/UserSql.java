package mybatis.mapper.mappers;

import java.util.List;

public class UserSql {
    public static String byName(String name) {
        if (name == null || name.isBlank()) {
            return "SELECT * FROM user";
        }
        return "SELECT * FROM user WHERE name = #{name}";
    }

    public static String byIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("ids must not be empty");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM user WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("#{ids[").append(i).append("]}");
        }
        sql.append(")");
        return sql.toString();
    }
}
