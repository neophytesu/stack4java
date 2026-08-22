package mysql.ast.parser.token;

import mysql.ast.parser.SqlParseException;

import java.util.List;

public class TokenStream {
    private final List<Token> tokens;
    private int index = 0;

    public TokenStream(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Token peek() {
        return tokens.get(index);
    }

    public Token next() {
        Token token = tokens.get(index);
        if (token.type() != TokenType.EOF) {
            index++;
        }
        return token;
    }

    public Token expect(TokenType type) {
        Token token = peek();
        if (token.type() != type) {
            throw new SqlParseException("期望" + type + ",实际上" + token.type() + "（" + token.lexeme() + "）");
        }
        return next();
    }

    public String expectIdentifier() {
        Token t = expect(TokenType.IDENTIFIER);
        return t.lexeme();
    }

    public Object expectLiteralValue() {
        Token t = peek();
        return switch (t.type()) {
            case INT_LITERAL, STRING_LITERAL, BOOLEAN_LITERAL -> next().value();
            default -> throw new SqlParseException("期望字面量，实际是 " + t.type());
        };
    }

    public boolean match(TokenType tokenType) {
        if (peek().type() == tokenType) {
            next();
            return true;
        }
        return false;
    }

    public boolean check(TokenType tokenType) {
        return peek().type() == tokenType;
    }

    public EqCondition parseEqCondition() {
        String column = expectIdentifier();
        expect(TokenType.EQ);
        Object value = expectLiteralValue();
        return new EqCondition(column, value);
    }
}
