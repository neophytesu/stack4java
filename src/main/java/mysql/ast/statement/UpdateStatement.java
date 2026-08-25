package mysql.ast.statement;

import mysql.ast.expr.Expr;

import java.util.List;

public record UpdateStatement(
        String schemaName,
        String tableName,
        List<Assignment> assignments,
        Expr where
) implements ManipulateStatement {
}
