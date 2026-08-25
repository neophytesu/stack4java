package mysql.ast.parser;

import mysql.ast.parser.token.Token;
import mysql.ast.parser.token.TokenType;

import java.util.Set;

public class SqlLexerSupport {
    private static final Set<String> KEYWORDS = Set.of(
            "INSERT", "INTO", "VALUES",
            "SELECT", "FROM", "WHERE",
            "UPDATE", "SET", "DELETE",
            "CREATE", "SCHEMA", "TABLE", "USE",
            "PRIMARY", "KEY",
            "INT", "VARCHAR", "BOOLEAN",
            "TRUE", "FALSE",
            "AND", "OR",
            "IS", "NOT", "NULL"
    );


    public static String preprocess(String s) {
        s = s.trim();
        if (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        return s.replaceAll("\\s+", " ");
    }

    public static boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    public static Token readWord(String sql, int start) {
        int i = start;
        while (i < sql.length() && isIdentifierPart(sql.charAt(i))) {
            i++;
        }
        String word = sql.substring(start, i);
        String upperWord = word.toUpperCase();
        switch (upperWord) {
            case "TRUE" -> {
                return Token.booleanLiteral(true);
            }
            case "FALSE" -> {
                return Token.booleanLiteral(false);
            }
            case "NULL" -> {
                return Token.nullLiteral();
            }
        }
        if (KEYWORDS.contains(upperWord)) {
            return Token.keyword(TokenType.valueOf(upperWord));
        }
        return Token.identifier(word);
    }

    private static boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    public static Token readNumber(String sql, int start) {
        int i = start;
        while (i < sql.length() && Character.isDigit(sql.charAt(i))) {
            i++;
        }
        String number = sql.substring(start, i);
        return Token.intLiteral(Integer.parseInt(number));
    }

    public static Token readString(String sql, int start) {
        StringBuilder sb = new StringBuilder();
        int i = start + 1;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (c == '\'') {
                if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    sb.append('\'');
                    i += 2;
                    continue;
                }
                return Token.stringLiteral(sb.toString());
            }
            sb.append(c);
            i++;
        }
        throw new SqlParseException("字符串引号未闭环");
    }

    public static int afterString(String sql, int start) {
        int i = start + 1;
        while (i < sql.length()) {
            if (sql.charAt(i) == '\'' && !(i + 1 < sql.length() && sql.charAt(i + 1) == '\'')) {
                return i + 1;
            }
            if (sql.charAt(i) == '\'' && sql.charAt(i + 1) == '\'') {
                i += 2;
            } else {
                i += 1;
            }
        }
        throw new SqlParseException("字符串引号未闭环");
    }
}
