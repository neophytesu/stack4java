package mysql.ast.statement;

public sealed interface ManipulateStatement extends Statement permits
        InsertStatement,
        UpdateStatement,
        DeleteStatement{
}
