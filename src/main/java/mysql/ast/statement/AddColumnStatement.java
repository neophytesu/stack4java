package mysql.ast.statement;

import mysql.storage.Column;

public record AddColumnStatement(String schemaName, String tableName, Column column) implements AlterTableStatement {
}
