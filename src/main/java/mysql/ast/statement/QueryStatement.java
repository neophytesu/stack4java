package mysql.ast.statement;

public sealed interface QueryStatement extends Statement permits
        SelectAllStatement,
        SelectWhereStatement,
        SelectColumnsStatement {
}
