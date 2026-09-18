package mysql.ast.statement;

public record SavepointStatement(String name) implements TransactionStatement {
}
