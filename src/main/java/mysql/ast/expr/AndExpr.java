package mysql.ast.expr;

public record AndExpr(Expr left, Expr right) implements Expr {
}
