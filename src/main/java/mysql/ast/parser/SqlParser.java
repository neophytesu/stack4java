package mysql.ast.parser;

import mysql.ast.expr.Expr;
import mysql.ast.parser.token.TokenStream;
import mysql.ast.parser.token.TokenType;
import mysql.ast.statement.*;
import mysql.core.EngineContext;
import mysql.storage.Column;
import mysql.storage.ColumnType;
import mysql.storage.Table;

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
        if (stream.match(TokenType.WHERE)) {
            Expr where = stream.parseWhere();
            stream.expect(TokenType.EOF);
            return new DeleteWhereStatement(currentSchemaName(), tableName, where);
        }
        stream.expect(TokenType.EOF);
        return new DeleteAllStatement(currentSchemaName(), tableName);
    }

    private UpdateStatement parseUpdate(TokenStream stream) {
        stream.expect(TokenType.UPDATE);
        String tableName = stream.expectIdentifier();
        stream.expect(TokenType.SET);
        String setColumn = stream.expectIdentifier();
        stream.expect(TokenType.EQ);
        Object newValue = stream.expectLiteralValue();
        stream.expect(TokenType.WHERE);
        Expr where = stream.parseWhere();
        stream.expect(TokenType.EOF);
        return new UpdateWhereStatement(currentSchemaName(), tableName, setColumn, newValue, where);
    }


    private QueryStatement parseSelect(TokenStream stream) {
        stream.expect(TokenType.SELECT);
        List<String> columns;
        if (stream.match(TokenType.STAR)) {
            columns = null;
        } else {
            columns = parseColumnList(stream);
        }
        stream.expect(TokenType.FROM);
        String tableName = stream.expectIdentifier();
        Expr where = null;
        if (stream.match(TokenType.WHERE)) {
            where = stream.parseWhere();
        }
        stream.expect(TokenType.EOF);
        return new SelectStatement(currentSchemaName(), tableName, columns, where);
    }

    private List<String> parseColumnList(TokenStream stream) {
        List<String> columns = new ArrayList<>();
        do {
            columns.add(stream.expectIdentifier());
        } while (stream.match(TokenType.COMMA));
        return columns;
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
        if (stream.match(TokenType.SCHEMA)) {
            String schemaName = stream.expectIdentifier();
            stream.expect(TokenType.EOF);
            return new CreateSchemaStatement(schemaName);
        }
        if (stream.match(TokenType.TABLE)) {
            return parseCreateTable(stream);
        }
        throw new SqlParseException("CREATE 后期望 SCHEMA 或 TABLE");
    }

    private DefineStatement parseCreateTable(TokenStream stream) {
        String tableName = stream.expectIdentifier();
        stream.expect(TokenType.LPAREN);
        List<Column> columns = new ArrayList<>();
        String primaryKeyColumn = null;
        do {
            if (stream.check(TokenType.PRIMARY)) {
                stream.expect(TokenType.PRIMARY);
                stream.expect(TokenType.KEY);
                stream.expect(TokenType.LPAREN);
                primaryKeyColumn = stream.expectIdentifier();
                stream.expect(TokenType.RPAREN);
                break;
            }
            String columnName = stream.expectIdentifier();
            ColumnType type = parseColumnType(stream);
            columns.add(new Column(columnName, type));
        } while (stream.match(TokenType.COMMA));
        stream.expect(TokenType.RPAREN);
        stream.expect(TokenType.EOF);
        if (primaryKeyColumn == null) {
            throw new SqlParseException("必须指定主键");
        }
        int pkIdx = findColumnIndex(columns, primaryKeyColumn);
        Table table = new Table();
        table.setTableName(tableName);
        table.setColumns(columns);
        table.setPrimaryIdx(pkIdx);
        table.setRows(new ArrayList<>());
        return new CreateTableStatement(currentSchemaName(), table);
    }

    private int findColumnIndex(List<Column> columns, String name) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getColumnName().equals(name)) {
                return i;
            }
        }
        throw new SqlParseException("主键列不存在：" + name);
    }

    private ColumnType parseColumnType(TokenStream stream) {
        TokenType type = stream.peek().type();
        return switch (type) {
            case INT -> {
                stream.next();
                yield ColumnType.INTEGER;
            }
            case VARCHAR -> {
                stream.next();
                yield ColumnType.VARCHAR;
            }
            case BOOLEAN -> {
                stream.next();
                yield ColumnType.BOOLEAN;
            }
            default -> throw new SqlParseException("不支持的类型：" + type);
        };
    }

}
