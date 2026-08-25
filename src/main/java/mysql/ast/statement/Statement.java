package mysql.ast.statement;

public sealed interface Statement permits ManipulateStatement, QueryStatement, DefineStatement
{

}
