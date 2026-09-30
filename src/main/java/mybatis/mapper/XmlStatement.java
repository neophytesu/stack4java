package mybatis.mapper;

import java.util.Map;

public record XmlStatement(String rawSql, boolean select, Map<String, String> columnByProperty) {
}
