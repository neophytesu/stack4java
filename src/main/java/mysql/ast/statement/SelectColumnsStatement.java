package mysql.ast.statement;

import java.util.List;

public record SelectColumnsStatement(
        String schemaName,
        String tableName,
        List<String> columnNames
) implements QueryStatement {}