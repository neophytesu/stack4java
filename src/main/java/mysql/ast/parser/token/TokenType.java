package mysql.ast.parser.token;

public enum TokenType {
    INSERT, INTO, VALUES,
    SELECT, FROM, WHERE,
    UPDATE, SET, DELETE,
    CREATE, SCHEMA, TABLE, USE,
    PRIMARY, KEY,

    INT, VARCHAR, BOOLEAN,

    IDENTIFIER,
    INT_LITERAL,
    STRING_LITERAL,
    BOOLEAN_LITERAL,

    STAR,
    COMMA,
    LPAREN, RPAREN,
    EQ, NE, LT, LE, GT, GE,
    IS, NULL,
    AND, OR, NOT,
    ORDER, BY, ASC, DESC,
    LIMIT, OFFSET,
    SEMICOLON,

    EOF
}
