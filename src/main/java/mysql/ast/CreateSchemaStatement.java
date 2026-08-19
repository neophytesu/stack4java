package mysql.ast;

public record CreateSchemaStatement(String schemaName) implements Statement {
}
