package mysql.ast.statement;

public record CreateSchemaStatement(String schemaName) implements DefineStatement {
}
