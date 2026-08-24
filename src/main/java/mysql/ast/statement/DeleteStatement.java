package mysql.ast.statement;

import mysql.ast.expr.Expr;

public record DeleteStatement(
        String schemaName,
        String tableName,
        Expr where
) implements ManipulateStatement {}