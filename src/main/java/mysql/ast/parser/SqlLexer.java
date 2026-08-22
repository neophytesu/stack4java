package mysql.ast.parser;

import mysql.ast.parser.token.Token;
import mysql.ast.parser.token.TokenType;

import java.util.ArrayList;
import java.util.List;

import static mysql.ast.parser.SqlLexerSupport.*;

public class SqlLexer {

    public List<Token> tokenize(String sql) {
        sql = SqlLexerSupport.preprocess(sql);
        List<Token> tokens = new ArrayList<>();
        int pos = 0;
        while (pos < sql.length()) {
            char c = sql.charAt(pos);
            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }
            if (c == ',') {
                tokens.add(Token.symbol(TokenType.COMMA, ','));
                pos++;
                continue;
            }
            if (c == '(') {
                tokens.add(Token.symbol(TokenType.LPAREN, '('));
                pos++;
                continue;
            }
            if (c == ')') {
                tokens.add(Token.symbol(TokenType.RPAREN, ')'));
                pos++;
                continue;
            }
            if (c == '=') {
                tokens.add(Token.symbol(TokenType.EQ, '='));
                pos++;
                continue;
            }
            if (c == '*') {
                tokens.add(Token.symbol(TokenType.STAR, '*'));
                pos++;
                continue;
            }
            if (c == ';') {
                tokens.add(Token.symbol(TokenType.SEMICOLON, ';'));
                pos++;
                continue;
            }
            if (c == '\'') {
                tokens.add(readString(sql, pos));
                pos = afterString(sql, pos);
                continue;
            }
            if (Character.isDigit(c)) {
                Token t = readNumber(sql, pos);
                tokens.add(t);
                pos += t.lexeme().length();
                continue;
            }
            if (isIdentifierStart(c)) {
                Token t = readWord(sql, pos);
                tokens.add(t);
                pos += t.lexeme().length();
                continue;
            }
            throw new SqlParseException("无法识别的字符：" + c);
        }
        tokens.add(Token.eof());
        return tokens;
    }
}
