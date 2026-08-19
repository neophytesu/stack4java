package mysql.ast.statement;

public record UseSchemaStatement(String schemaName) implements Statement{
}
