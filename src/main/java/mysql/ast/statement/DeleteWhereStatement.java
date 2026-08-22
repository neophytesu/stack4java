package mysql.ast.statement;

import mysql.ast.expr.Expr;

public record DeleteWhereStatement(String schemaName, String tableName, Expr where) implements UpdateStatement {
}
