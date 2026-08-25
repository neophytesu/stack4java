package mysql.ast.parser.token;

import mysql.ast.expr.*;
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
            case NULL -> {
                next();
                yield null;
            }
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

    public Expr parseWhere() {
        return parseOrExpr();
    }

    public Expr parseOrExpr() {
        Expr left = parseAndExpr();
        while (match(TokenType.OR)) {
            left = new OrExpr(left, parseAndExpr());
        }
        return left;
    }

    public Expr parseAndExpr() {
        Expr left = parsePrimaryExpr();
        while (match(TokenType.AND)) {
            left = new AndExpr(left, parsePrimaryExpr());
        }
        return left;
    }

    private Expr parsePrimaryExpr() {
        if (match(TokenType.LPAREN)) {
            Expr inner = parseOrExpr();
            expect(TokenType.RPAREN);
            return inner;
        }
        String column = expectIdentifier();
        if (match(TokenType.IS)) {
            boolean negated = match(TokenType.NOT);
            expect(TokenType.NULL);
            return new IsNullExpr(column, negated);
        }
        CompareOp op = parseCompareOp();
        Object value = expectLiteralValue();
        return new CompareExpr(column, op, value);
    }

    private CompareOp parseCompareOp() {
        return switch (next().type()) {
            case EQ -> CompareOp.EQ;
            case NE -> CompareOp.NE;
            case LT -> CompareOp.LT;
            case GT -> CompareOp.GT;
            case LE -> CompareOp.LE;
            case GE -> CompareOp.GE;
            default -> throw new SqlParseException("期望比较运算符");
        };
    }
}
