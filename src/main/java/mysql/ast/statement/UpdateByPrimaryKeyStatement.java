package mysql.ast.statement;

public record UpdateByPrimaryKeyStatement(
        String schemaName,
        String tableName,
        Object pkValue,
        String columnName,
        Object newValue
) implements UpdateStatement {
}