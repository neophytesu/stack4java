package mysql.ast.statement;

public sealed interface Statement permits UseSchemaStatement,
        UseTableStatement,
        CreateSchemaStatement,
        CreateTableStatement,
        InsertStatement,
        SelectWhereStatement,
        SelectAllStatement,
        UpdateByPrimaryKeyStatement,
        DeleteByPrimaryKeyStatement,
        DeleteAllStatement,
        SelectColumnsStatement
{

}
