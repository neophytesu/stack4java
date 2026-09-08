package mysql.ast.statement;

public sealed interface AlterTableStatement extends DefineStatement permits AddColumnStatement,DropColumnStatement{
}
