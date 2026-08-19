package mysql.ast;

import mysql.storage.Table;

public record CreateTableStatement(String schemaName, Table table) implements Statement {
}
