package mysql.ast.expr.compare;

public sealed interface Expr permits AndExpr, CompareExpr, IsNullExpr, OrExpr {
}
