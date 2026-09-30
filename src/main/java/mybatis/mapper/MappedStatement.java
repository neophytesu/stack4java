package mybatis.mapper;

import jdbc.RowMapper;

import java.lang.reflect.Method;
import java.util.Map;

public record MappedStatement(
        String rawSql,
        Method sqlProvider,
        boolean select,
        boolean many,
        RowMapper<?> rowMapper,
        boolean useGeneratedKeys,
        Map<String, String> columnByProperty,
        String namespace,
        boolean cache) {
}
