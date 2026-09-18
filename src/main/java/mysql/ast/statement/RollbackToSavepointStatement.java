package mysql.ast.statement;

public record RollbackToSavepointStatement(String name) implements TransactionStatement {
}
