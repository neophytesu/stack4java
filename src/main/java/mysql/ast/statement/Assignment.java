package mysql.ast.statement;

import mysql.ast.expr.value.ValueExpr;

public record Assignment(String columnName, ValueExpr value) {
}
