package mysql.ast.expr.value;

import mysql.ast.expr.compare.ParamPlaceholder;
import mysql.base.MysqlExecuteException;
import mysql.storage.Row;
import mysql.storage.Table;
import mysql.utils.MysqlUtil;

import java.util.Collections;

public final class ValueExprEvaluator {
    private ValueExprEvaluator() {
    }

    public static Object eval(ValueExpr expr, Row row, Table table) {
        return switch (expr) {
            case ValueLiteral v -> evalLiteral(v);
            case ColumnRef c -> evalColumn(c, row, table);
            case BinaryValueExpr b -> evalBinary(b, row, table);
        };
    }

    private static Object evalBinary(BinaryValueExpr binaryValueExpr, Row row, Table table) {
        Object left = eval(binaryValueExpr.left(), row, table);
        Object right = eval(binaryValueExpr.right(), row, table);
        if (left == null || right == null) {
            throw MysqlExecuteException.NULL_NOT_SUPPORTED_COMPUTE();
        }
        if (!(left instanceof Integer) || !(right instanceof Integer)) {
            throw MysqlExecuteException.WRONG_TYPE_TO_COMPUTE();
        }
        return switch (binaryValueExpr.op()) {
            case ADD -> (Integer) left + (Integer) right;
            case SUB -> (Integer) left - (Integer) right;
        };
    }

    private static Object evalColumn(ColumnRef columnRef, Row row, Table table) {
        int idx = MysqlUtil.columnName2Index(Collections.singletonList(columnRef.columnName()), table.getColumns()).getFirst();
        return row.getValues()[idx];
    }

    private static Object evalLiteral(ValueLiteral valueLiteral) {
        Object value = valueLiteral.value();
        if (value instanceof ParamPlaceholder) {
            throw MysqlExecuteException.NEED_TO_PREPARE();
        }
        return value;
    }
}
