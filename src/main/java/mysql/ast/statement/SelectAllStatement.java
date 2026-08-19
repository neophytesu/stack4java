package mysql.ast.statement;

public record SelectAllStatement(String schemaName, String tableName) implements Statement {
}
