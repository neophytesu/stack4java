package mysql.ast.expr.value;

public record ColumnRef(String columnName) implements ValueExpr {
}
