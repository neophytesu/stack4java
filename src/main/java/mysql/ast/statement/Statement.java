package mysql.ast.statement;

public sealed interface Statement permits DefineStatement, ManipulateStatement, QueryStatement, TransactionStatement
{

}
