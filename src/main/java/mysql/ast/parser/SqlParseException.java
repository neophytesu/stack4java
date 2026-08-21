package mysql.ast.parser;

public class SqlParseException extends RuntimeException {
    public SqlParseException(String message) {
        super(message);
    }
}
