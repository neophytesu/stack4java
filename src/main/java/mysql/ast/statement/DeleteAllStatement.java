package mysql.ast.statement;

public record DeleteAllStatement(
        String schemaName,
        String tableName
) implements Statement {}