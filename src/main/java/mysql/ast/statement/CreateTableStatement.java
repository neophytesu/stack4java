package mysql.ast.statement;

import mysql.storage.Table;

public record CreateTableStatement(String schemaName, Table table) implements UpdateStatement {
}
