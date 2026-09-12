package mysql.ast.expr.compare;

public record IsNullExpr(String column, boolean negated) implements Expr {
}
