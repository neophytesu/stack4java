package mysql.ast.expr.compare;

public record OrExpr(Expr left, Expr right) implements Expr {
}
