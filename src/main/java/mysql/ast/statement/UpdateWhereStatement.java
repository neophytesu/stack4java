package mysql.ast.statement;

import mysql.ast.expr.Expr;

public record UpdateWhereStatement(
        String schemaName,
        String tableName,
        String columnName,
        Object newValue,
        Expr where
) implements UpdateStatement {
}
