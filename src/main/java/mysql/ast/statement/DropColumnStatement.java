package mysql.ast.statement;

public record DropColumnStatement(String schemaName, String tableName,
                                  String columnName) implements AlterTableStatement {
}
