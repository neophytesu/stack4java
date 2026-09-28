package mybatis;

public record BoundSql(String jdbcSql, Object[] args) {
}
