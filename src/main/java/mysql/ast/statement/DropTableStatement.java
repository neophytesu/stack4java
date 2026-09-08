package mysql.ast.statement;

public record DropTableStatement(String schemaName, String tableName) implements DefineStatement {
}
