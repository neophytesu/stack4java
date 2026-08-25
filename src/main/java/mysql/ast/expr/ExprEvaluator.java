package mysql.ast.expr;

import mysql.base.MysqlExecuteException;
import mysql.storage.Column;
import mysql.storage.Row;
import mysql.storage.Table;
import mysql.utils.MysqlUtil;

import java.util.List;

public final class ExprEvaluator {
    public static boolean eval(Expr expr, Row row, Table table) {
        return switch (expr) {
            case AndExpr e -> eval(e.left(), row, table) && eval(e.right(), row, table);
            case OrExpr e -> eval(e.left(), row, table) || eval(e.right(), row, table);
            case CompareExpr e -> evalCompare(e, row, table);
            case IsNullExpr e -> evalIsNull(e, row, table);
        };
    }

    private static boolean evalIsNull(IsNullExpr e, Row row, Table table) {
        int idx = columnIndex(table, e.column());
        Object value = cell(row, idx);
        boolean isNull = value == null;
        return e.negated() != isNull;
    }

    private static int columnIndex(Table table, String columnName) {
        List<Column> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (columnName.equals(columns.get(i).getColumnName())) {
                return i;
            }
        }
        throw MysqlExecuteException.COLUMN_NOT_EXITED(columnName);
    }

    private static Object cell(Row row, int idx) {
        return row.getValues()[idx];
    }

    private static void checkLiteral(Column column, Object value) {
        if (column.getColumnType().refuse(value)) {
            throw MysqlExecuteException.COLUMN_TYPE_NOT_MATCHED(column.getColumnName(), value);
        }
    }

    private static boolean evalCompare(CompareExpr e, Row row, Table table) {
        int idx = columnIndex(table, e.column());
        Column col = table.getColumns().get(idx);
        checkLiteral(col, e.value());
        return MysqlUtil.compare(cell(row, idx), e.value(), col.getColumnType(), e.op());
    }
}
