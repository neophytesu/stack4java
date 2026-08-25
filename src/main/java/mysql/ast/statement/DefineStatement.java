package mysql.ast.statement;

public sealed interface DefineStatement extends Statement permits
        UseSchemaStatement,
        UseTableStatement,
        CreateSchemaStatement,
        CreateTableStatement {
}
