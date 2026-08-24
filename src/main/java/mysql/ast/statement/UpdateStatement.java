package mysql.ast.statement;

import mysql.ast.expr.Expr;

public record UpdateStatement(
        String schemaName,
        String tableName,
        String columnName,
        Object newValue,
        Expr where
) implements ManipulateStatement {
}
