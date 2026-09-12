package mysql.ast.expr.value;

public sealed interface ValueExpr permits ValueLiteral, ColumnRef, BinaryValueExpr {
}
