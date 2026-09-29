package mybatis.mapper.mappers;

public class UserSql {
    public static String byName(String name) {
        if (name == null || name.isBlank()) {
            return "SELECT * FROM user";
        }
        return "SELECT * FROM user WHERE name = #{name}";
    }
}
