package mybatis.mapper;

import jdbc.RowMapper;

import java.lang.reflect.Method;

public record MappedStatement(
        String rawSql,
        Method sqlProvider,
        boolean select,
        boolean many,
        RowMapper<?> rowMapper,
        boolean useGeneratedKeys) {
}
