package mysql.ast.expr.compare;

public record CompareExpr(String column, CompareOp op, Object value) implements Expr {
}
