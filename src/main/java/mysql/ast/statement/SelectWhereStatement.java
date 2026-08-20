package mysql.ast.statement;

public record SelectWhereStatement(String schemaName,String tableName,String columnName,Object value) implements QueryStatement {
}
