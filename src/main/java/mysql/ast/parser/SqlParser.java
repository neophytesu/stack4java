package mysql.ast.parser;

import mysql.ast.expr.Expr;
import mysql.ast.parser.token.TokenStream;
import mysql.ast.parser.token.TokenType;
import mysql.ast.statement.*;
import mysql.ast.statement.CreateSchemaStatement;
import mysql.ast.statement.CreateTableStatement;
import mysql.ast.statement.SelectStatement;
import mysql.ast.statement.DefineStatement;
import mysql.ast.statement.ManipulateStatement;
import mysql.ast.statement.QueryStatement;
import mysql.core.EngineContext;
import mysql.storage.Column;
import mysql.storage.ColumnType;
import mysql.storage.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static mysql.ast.parser.token.TokenType.*;

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

    public Statement parse(String sql) {
        TokenStream stream = new TokenStream(lexer.tokenize(sql));
        TokenType type = stream.peek().type();
        return switch (type) {
            case SELECT -> parseDql(stream, type);
            case INSERT, UPDATE, DELETE -> parseDml(stream, type);
            case CREATE, USE -> parseDdl(stream, type);
            default -> throw new SqlParseException("不支持: " + type);
        };
    }

    private ManipulateStatement parseDml(TokenStream stream, TokenType type) {
        return switch (type) {
            case INSERT -> parseInsert(stream);
            case UPDATE -> parseUpdate(stream);
            case DELETE -> parseDelete(stream);
            default -> throw new SqlParseException("不支持: " + type);
        };
    }

    private QueryStatement parseDql(TokenStream stream, TokenType type) {
        return switch (type) {
            case SELECT -> parseSelect(stream);
            default -> throw new SqlParseException("不支持: " + type);
        };
    }

    private DefineStatement parseDdl(TokenStream stream, TokenType type) {
        return switch (type) {
            case CREATE -> parseCreate(stream);
            case USE -> parseUse(stream);
            default -> throw new SqlParseException("不支持: " + type);
        };
    }

    private ManipulateStatement parseDelete(TokenStream stream) {
        stream.expect(DELETE);
        stream.expect(FROM);
        String tableName = stream.expectIdentifier();
        Expr where = null;
        if (stream.match(WHERE)) {
            where = stream.parseWhere();
        }
        stream.expect(EOF);
        return new DeleteStatement(currentSchemaName(), tableName, where);
    }

    private ManipulateStatement parseUpdate(TokenStream stream) {
        stream.expect(UPDATE);
        String tableName = stream.expectIdentifier();
        stream.expect(SET);
        List<Assignment> assignments = new ArrayList<>();
        do {
            String setColumn = stream.expectIdentifier();
            stream.expect(EQ);
            Object newValue = stream.expectLiteralValue();
            assignments.add(new Assignment(setColumn, newValue));
        } while (stream.match(COMMA));
        Expr where = null;
        if (stream.match(WHERE)) {
            where = stream.parseWhere();
        }
        stream.expect(EOF);
        return new UpdateStatement(currentSchemaName(), tableName, assignments, where);
    }


    private QueryStatement parseSelect(TokenStream stream) {
        stream.expect(SELECT);
        List<String> columns;
        if (stream.match(STAR)) {
            columns = null;
        } else {
            columns = parseColumnList(stream);
        }
        stream.expect(FROM);
        String tableName = stream.expectIdentifier();
        Expr where = null;
        if (stream.match(WHERE)) {
            where = stream.parseWhere();
        }
        List<OrderByItem> orderByItems = null;
        if (stream.match(ORDER)) {
            stream.expect(BY);
            orderByItems = parseOrderByList(stream);
        }
        Integer limit = null;
        Integer offset = null;
        if (stream.match(LIMIT)) {
            limit = stream.expectIntLiteral();
            if (stream.match(OFFSET)) {
                offset = stream.expectIntLiteral();
            }
        }
        stream.expect(EOF);
        return new SelectStatement(currentSchemaName(), tableName, columns, where, orderByItems, limit, offset);
    }

    private List<OrderByItem> parseOrderByList(TokenStream stream) {
        List<OrderByItem> list = new ArrayList<>();
        do {
            String column = stream.expectIdentifier();
            boolean asc = true;
            if (stream.match(DESC)) {
                asc = false;
            } else {
                stream.match(ASC);
            }
            list.add(new OrderByItem(column, asc));
        } while (stream.match(COMMA));
        return list;
    }

    private List<String> parseColumnList(TokenStream stream) {
        List<String> columns = new ArrayList<>();
        do {
            columns.add(stream.expectIdentifier());
        } while (stream.match(COMMA));
        return columns;
    }

    private ManipulateStatement parseInsert(TokenStream stream) {
        stream.expect(INSERT);
        stream.expect(INTO);
        String tableName = stream.expectIdentifier();
        List<String> columnNames = null;
        if (stream.match(LPAREN)) {
            columnNames = parseColumnList(stream);
            stream.expect(RPAREN);
        }
        stream.expect(VALUES);
        stream.expect(LPAREN);
        List<Object> values = new ArrayList<>();
        do {
            values.add(stream.expectLiteralValue());
        } while (stream.match(COMMA));
        stream.expect(RPAREN);
        stream.expect(EOF);
        if (columnNames != null && columnNames.size() != values.size()) {
            throw new SqlParseException("列数与值数不匹配");
        }
        return new InsertStatement(currentSchemaName(), tableName, columnNames, values);
    }

    private DefineStatement parseUse(TokenStream stream) {
        stream.expect(USE);
        String schemaName = stream.expectIdentifier();
        stream.expect(EOF);
        return new UseSchemaStatement(schemaName);
    }

    private DefineStatement parseCreate(TokenStream stream) {
        stream.expect(CREATE);
        if (stream.match(SCHEMA)) {
            String schemaName = stream.expectIdentifier();
            stream.expect(EOF);
            return new CreateSchemaStatement(schemaName);
        }
        if (stream.match(TABLE)) {
            return parseCreateTable(stream);
        }
        throw new SqlParseException("CREATE 后期望 SCHEMA 或 TABLE");
    }

    private DefineStatement parseCreateTable(TokenStream stream) {
        String tableName = stream.expectIdentifier();
        stream.expect(LPAREN);
        List<Column> columns = new ArrayList<>();
        String primaryKeyColumn = null;
        Set<String> autoIncrementColumns = new HashSet<>();
        do {
            if (stream.check(PRIMARY)) {
                stream.expect(PRIMARY);
                stream.expect(KEY);
                stream.expect(LPAREN);
                primaryKeyColumn = stream.expectIdentifier();
                autoIncrementColumns.remove(primaryKeyColumn);
                stream.expect(RPAREN);
                break;
            }
            String columnName = stream.expectIdentifier();
            ColumnType type = parseColumnType(stream);
            if (stream.match(AUTO_INCREMENT)) {
                if (!type.isSupportedAutoIncrement()) {
                    throw new SqlParseException(columnName + "列的类型不支持自增");
                }
                autoIncrementColumns.add(columnName);
                columns.add(new Column(columnName, type, true));
            } else {
                columns.add(new Column(columnName, type, false));
            }
        } while (stream.match(COMMA));
        if (!autoIncrementColumns.isEmpty()) {
            throw new SqlParseException("只有主键列才能自增");
        }
        stream.expect(RPAREN);
        stream.expect(EOF);
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
