package mysql.ast;

public record UseSchemaStatement(String schemaName) implements Statement{
}
