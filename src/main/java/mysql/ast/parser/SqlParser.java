package mysql.ast.parser;

import mysql.ast.parser.token.EqCondition;
import mysql.ast.parser.token.TokenStream;
import mysql.ast.parser.token.TokenType;
import mysql.ast.statement.*;
import mysql.core.EngineContext;

import java.util.ArrayList;
import java.util.List;

public class SqlParser {
    private final EngineContext context;
    private final SqlLexer lexer = new SqlLexer();

    public SqlParser(EngineContext context) {
        this.context = context;
    }

    private String currentSchemaName() {
        if (context.getCurrentSchema() == null) {
            throw new SqlParseException("请先选择schema");
        }
        return context.getCurrentSchema().getSchemaName();
    }

    public UpdateStatement parseDml(String sql) {
        TokenStream stream = getTokenStream(sql);
        TokenType type = stream.peek().type();
        return switch (type) {
            case INSERT -> parseInsert(stream);
            case UPDATE -> parseUpdate(stream);
            case DELETE -> parseDelete(stream);
            default -> throw new SqlParseException("不支持: " + type);
        };
    }

    private TokenStream getTokenStream(String sql) {
        return new TokenStream(lexer.tokenize(sql));
    }

    public QueryStatement parseDql(String sql) {
        TokenStream stream = getTokenStream(sql);
        TokenType type = stream.peek().type();
        return switch (type) {
            case SELECT -> parseSelect(stream);
            default -> throw new SqlParseException("不支持: " + type);
        };
    }

    public DefineStatement parseDdl(String sql) {
        TokenStream stream = getTokenStream(sql);
        TokenType type = stream.peek().type();
        return switch (type) {
            case CREATE -> parseCreate(stream);
            case USE -> parseUse(stream);
            default -> throw new SqlParseException("不支持: " + type);
        };
    }

    private UpdateStatement parseDelete(TokenStream stream) {
        stream.expect(TokenType.DELETE);
        stream.expect(TokenType.FROM);
        String tableName = stream.expectIdentifier();
        stream.expect(TokenType.WHERE);
        EqCondition eqCondition = stream.parseEqCondition();
        stream.expect(TokenType.EOF);
        return new DeleteByPrimaryKeyStatement(currentSchemaName(), tableName, eqCondition.value());
    }

    private UpdateStatement parseUpdate(TokenStream stream) {
        stream.expect(TokenType.UPDATE);
        String tableName = stream.expectIdentifier();
        stream.expect(TokenType.SET);
        String setColumn = stream.expectIdentifier();
        stream.expect(TokenType.EQ);
        Object newValue = stream.expectLiteralValue();
        stream.expect(TokenType.WHERE);
        EqCondition eqCondition = stream.parseEqCondition();
        stream.expect(TokenType.EOF);
        return new UpdateByPrimaryKeyStatement(currentSchemaName(), tableName, eqCondition.value(), setColumn, newValue);
    }


    private QueryStatement parseSelect(TokenStream stream) {
        stream.expect(TokenType.SELECT);
        stream.expect(TokenType.STAR);
        stream.expect(TokenType.FROM);
        String tableName = stream.expectIdentifier();
        if (stream.match(TokenType.WHERE)) {
            EqCondition eq = stream.parseEqCondition();
            stream.expect(TokenType.EOF);
            return new SelectWhereStatement(currentSchemaName(), tableName, eq.column(), eq.value());
        }
        stream.expect(TokenType.EOF);
        return new SelectAllStatement(currentSchemaName(), tableName);
    }

    private UpdateStatement parseInsert(TokenStream stream) {
        stream.expect(TokenType.INSERT);
        stream.expect(TokenType.INTO);
        String tableName = stream.expectIdentifier();
        stream.expect(TokenType.VALUES);
        stream.expect(TokenType.LPAREN);
        List<Object> values = new ArrayList<>();
        do {
            values.add(stream.expectLiteralValue());
        } while (stream.match(TokenType.COMMA));
        stream.expect(TokenType.RPAREN);
        stream.expect(TokenType.EOF);
        return new InsertStatement(currentSchemaName(), tableName, values);
    }

    private DefineStatement parseUse(TokenStream stream) {
        stream.expect(TokenType.USE);
        String schemaName = stream.expectIdentifier();
        stream.expect(TokenType.EOF);
        return new UseSchemaStatement(schemaName);
    }

    private DefineStatement parseCreate(TokenStream stream) {
        stream.expect(TokenType.CREATE);
        stream.expect(TokenType.SCHEMA);
        String schemaName = stream.expectIdentifier();
        stream.expect(TokenType.EOF);
        return new CreateSchemaStatement(schemaName);
    }

}
