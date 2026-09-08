package mysql.ast.parser;

import mysql.ast.statement.Statement;

public record ParsedSql(Statement statement, int paramCount) {
}
