package mysql.ast.statement;

public sealed interface TransactionStatement extends Statement permits BeginStatement, CommitStatement, ReleaseSavepointStatement, RollbackStatement, RollbackToSavepointStatement, SavepointStatement {
}
