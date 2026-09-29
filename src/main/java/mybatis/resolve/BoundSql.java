package mybatis.resolve;

public record BoundSql(String jdbcSql, Object[] args) {
}
