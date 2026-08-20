package mysql.ast.statement;

public sealed interface UpdateStatement extends Statement permits
        UseSchemaStatement,
        UseTableStatement,
        CreateSchemaStatement,
        CreateTableStatement,
        InsertStatement,
        UpdateByPrimaryKeyStatement,
        DeleteByPrimaryKeyStatement,
        DeleteAllStatement {
}
