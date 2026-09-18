package mysql.ast.statement;

public record ReleaseSavepointStatement(String name) implements TransactionStatement {
}
