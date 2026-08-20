package mysql.ast.statement;

public record DeleteByPrimaryKeyStatement(
        String schemaName,
        String tableName,
        Object pkValue
) implements UpdateStatement {}