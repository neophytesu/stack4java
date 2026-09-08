package mysql.core;

import mysql.ast.expr.*;
import mysql.ast.parser.SqlParseException;
import mysql.ast.statement.*;

import java.util.List;

public class PlaceholderResolver {

    public static Statement resolve(Statement stmt, Object[] params) {
        return switch (stmt) {
            case SelectStatement s -> resolveSelect(s, params);
            case InsertStatement s -> resolveInsert(s, params);
            case UpdateStatement s -> resolveUpdate(s, params);
            case DeleteStatement s -> resolveDelete(s, params);
            default -> throw new SqlParseException("不支持预处理的语句：" + stmt.getClass());
        };
    }

    private static Statement resolveDelete(DeleteStatement s, Object[] params) {
        Expr where = s.where() == null ? null : resolveExpr(s.where(), params);
        return new DeleteStatement(s.schemaName(), s.tableName(), where);
    }

    private static Statement resolveUpdate(UpdateStatement s, Object[] params) {
        List<Assignment> assignments = s.assignments().stream()
                .map(a -> new Assignment(a.columnName(), resolveValue(a.newValue(), params)))
                .toList();
        Expr where = s.where() == null ? null : resolveExpr(s.where(), params);
        return new UpdateStatement(s.schemaName(), s.tableName(), assignments, where);
    }

    private static Statement resolveInsert(InsertStatement s, Object[] params) {
        List<List<Object>> rows = s.rows().stream().map(row -> row.stream().map(v -> resolveValue(v, params)).toList()).toList();
        return new InsertStatement(s.schemaName(), s.tableName(), s.columnNames(), rows);
    }

    private static Statement resolveSelect(SelectStatement s, Object[] params) {
        Expr where = s.where() == null ? null : resolveExpr(s.where(), params);
        return new SelectStatement(s.schemaName(), s.tableName(), s.columns(), where, s.orderByItems(), s.limit(), s.offset());
    }

    private static Object resolveValue(Object value, Object[] params) {
        if (value instanceof ParamPlaceholder(int index)) {
            if (index < 0 || index >= params.length) {
                throw new SqlParseException("参数未绑定：index=" + index);
            }
            return params[index];
        }
        return value;
    }

    private static Expr resolveExpr(Expr expr, Object[] params) {
        return switch (expr) {
            case CompareExpr e -> new CompareExpr(e.column(), e.op(), resolveValue(e.value(), params));
            case AndExpr e -> new AndExpr(resolveExpr(e.left(), params), resolveExpr(e.right(), params));
            case OrExpr e -> new OrExpr(resolveExpr(e.left(), params), resolveExpr(e.right(), params));
            case IsNullExpr e -> e;
            default -> throw new SqlParseException("未知表达式：" + expr);
        };
    }
}
