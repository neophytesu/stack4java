package mysql.ast.statement;

public sealed interface DefineStatement extends Statement permits AlterTableStatement, CreateSchemaStatement, CreateTableStatement, DropTableStatement, UseSchemaStatement, UseTableStatement {
}
