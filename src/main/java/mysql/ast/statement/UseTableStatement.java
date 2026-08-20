package mysql.ast.statement;

public record UseTableStatement(String tableName) implements  UpdateStatement {
}
