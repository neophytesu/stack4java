package mysql.ast.expr;

public record IsNullExpr(String column, boolean negated) implements Expr {
}
