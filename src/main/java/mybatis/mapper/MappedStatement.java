package mybatis.mapper;

import jdbc.RowMapper;

public record MappedStatement(String rawSql, boolean select, boolean many, RowMapper<?> rowMapper) {
}
