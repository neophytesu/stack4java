package mysql.ast.statement;

public record Assignment(String columnName, Object newValue) {
}
