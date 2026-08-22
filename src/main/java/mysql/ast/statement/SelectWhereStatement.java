package mysql.ast.statement;

import mysql.ast.expr.Expr;

public record SelectWhereStatement(String schemaName, String tableName, Expr where) implements QueryStatement {
}
