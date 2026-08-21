package mysql.ast.parser;

import mysql.ast.statement.*;
import mysql.core.EngineContext;

import java.util.Arrays;
import java.util.List;

import static mysql.ast.parser.SqlParserUtil.*;

public class SqlParser {
    private final EngineContext context;

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
        sql = preprocess(sql);
        String keyword = firstKeyword(sql);
        return switch (keyword.toUpperCase()) {
            case "INSERT" -> parseInsert(sql);
            case "UPDATE" -> parseUpdate(sql);
            case "DELETE" -> parseDelete(sql);
            default -> throw new SqlParseException("不支持: " + keyword);
        };
    }

    public QueryStatement parseDql(String sql) {
        sql = preprocess(sql);
        String keyword = firstKeyword(sql);
        if (keyword.equalsIgnoreCase("SELECT")) {
            return parseSelect(sql);
        }
        throw new SqlParseException("不支持: " + keyword);
    }

    public DefineStatement parseDdl(String sql) {
        sql = preprocess(sql);
        String keyword = firstKeyword(sql);
        return switch (keyword.toUpperCase()) {
            case "CREATE" -> parseCreate(sql);
            case "USE" -> parseUse(sql);
            default -> throw new SqlParseException("不支持: " + keyword);
        };
    }

    private UpdateStatement parseDelete(String sql) {
        String tableName = between(sql, "DELETE FROM ", " WHERE ");
        String afterWhere = after(sql, " WHERE ");
        String[] wherePair = splitEquals(afterWhere);
        return new DeleteByPrimaryKeyStatement(currentSchemaName(), tableName.trim(), parseLiteral(wherePair[1]));
    }

    private UpdateStatement parseUpdate(String sql) {
        String afterUpdate = after(sql, "UPDATE ");
        String tableName = before(afterUpdate, " SET ");
        String afterSet = between(afterUpdate, " SET ", " WHERE ");
        String[] setPair = splitEquals(afterSet);
        String afterWhere = after(sql, " WHERE ");
        String[] wherePair = splitEquals(afterWhere);
        return new UpdateByPrimaryKeyStatement(currentSchemaName(), tableName.trim(), parseLiteral(wherePair[1]), setPair[0].trim(), parseLiteral(setPair[1]));
    }


    private QueryStatement parseSelect(String sql) {
        if (sql.contains(" WHERE ")) {
            String tableName = between(sql, "SELECT * FROM", " WHERE ").trim();
            String wherePart = after(sql, " WHERE ");
            String[] eq = splitEquals(wherePart);
            return new SelectWhereStatement(currentSchemaName(), tableName, eq[0].trim(), parseLiteral(eq[1].trim()));
        } else {
            String tableName = after(sql, "SELECT * FROM").trim();
            return new SelectAllStatement(currentSchemaName(), tableName);
        }
    }

    private UpdateStatement parseInsert(String sql) {
        String tableName = between(sql, "INSERT INTO ", " VALUES").trim();
        String valuesPart = betweenParentheses(sql, "VALUES");
        List<Object> values = parseValueList(valuesPart);
        return new InsertStatement(currentSchemaName(), tableName, values);
    }

    private DefineStatement parseUse(String sql) {
        List<String> parts = Arrays.asList(sql.split(" "));
        return new UseSchemaStatement(parts.get(1));
    }

    private DefineStatement parseCreate(String sql) {
        List<String> parts = Arrays.asList(sql.split(" "));
        if (parts.get(1).equalsIgnoreCase("SCHEMA")) {
            return new CreateSchemaStatement(parts.get(2));
        }
        throw new SqlParseException("sql CREATE语句存在错误");
    }

}
