package mysql.ast.statement;

import mysql.ast.expr.Expr;

import java.util.List;

public record SelectStatement(String schemaName, String tableName, List<String> columns,
                              Expr where, List<OrderByItem> orderByItems, Integer limit,
                              Integer offset) implements QueryStatement {
}
