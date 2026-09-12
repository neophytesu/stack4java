package mysql.ast.expr.value;

public record BinaryValueExpr(ValueExpr left, ArityOp op, ValueExpr right) implements ValueExpr {
}
