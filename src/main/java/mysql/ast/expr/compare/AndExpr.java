package mysql.ast.expr.compare;

public record AndExpr(Expr left, Expr right) implements Expr {
}
