package mysql.ast.parser.token;

public record Token(TokenType type, String lexeme, Object value) {

    public static Token keyword(TokenType type) {
        return new Token(type, type.name(), null);
    }

    public static Token identifier(String name) {
        return new Token(TokenType.IDENTIFIER, name, name);
    }

    public static Token intLiteral(int n) {
        return new Token(TokenType.INT_LITERAL, String.valueOf(n), n);
    }

    public static Token stringLiteral(String s) {
        return new Token(TokenType.STRING_LITERAL, "'" + s + "'", s);
    }

    public static Token booleanLiteral(boolean b) {
        return new Token(TokenType.BOOLEAN_LITERAL, String.valueOf(b), b);
    }

    public static Token symbol(TokenType type, char c) {
        return new Token(type, String.valueOf(c), null);
    }

    public static Token symbol(TokenType type, String s) {
        return new Token(type, s, null);
    }

    public static Token eof() {
        return new Token(TokenType.EOF, "", null);
    }
}
