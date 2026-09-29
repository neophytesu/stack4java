package mysql.ast.expr.compare;

import java.util.List;

public record InExpr(String column, List<Object> values, boolean negated) implements Expr {
}
