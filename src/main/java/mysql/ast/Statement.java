package mysql.ast;

public sealed interface Statement permits UseSchemaStatement,
        UseTableStatement,
        CreateSchemaStatement,
        CreateTableStatement,
        InsertStatement,
        SelectWhereStatement {

}
