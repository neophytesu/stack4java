package mysql.ast.expr;

public record CompareExpr(String column, CompareOp op, Object value) implements Expr {
}
