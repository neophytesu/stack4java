package mysql.ast;

public record SelectWhereStatement(String schemaName,String tableName,String columnName,Object value) implements Statement {
}
