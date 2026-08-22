package mysql.ast.statement;

public sealed interface UpdateStatement extends Statement permits
        InsertStatement,
        UpdateWhereStatement,
        DeleteAllStatement,
        DeleteWhereStatement{
}
