package mysql.utils;

import mysql.ast.expr.CompareOp;
import mysql.ast.statement.OrderByItem;
import mysql.base.MysqlExecuteException;
import mysql.storage.Column;
import mysql.storage.ColumnType;
import mysql.storage.Row;

import java.util.*;

public class MysqlUtil {

    public static Object deepCopyValue(Object value, ColumnType columnType) {
        if (value == null) {
            return null;
        }
        return columnType.copyValue(value);
    }

    public static List<Integer> columnName2Index(List<String> columnNames, List<Column> columns) {
        Map<String, Integer> columnIndexMap = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            columnIndexMap.put(columns.get(i).getColumnName(), i);
        }
        List<Integer> columnIndices = new ArrayList<>();
        for (String columnName : columnNames) {
            Integer index = columnIndexMap.get(columnName);
            if (index == null) {
                throw MysqlExecuteException.COLUMN_NOT_FIND(columnName);
            }
            columnIndices.add(index);
        }
        return columnIndices;
    }

    public static List<Object> deepCopy(List<Object> original, List<ColumnType> columnType) {
        if (original == null) {
            return new ArrayList<>();
        }
        List<Object> copy = new ArrayList<>();
        for (int i = 0; i < original.size(); i++) {
            copy.add(deepCopyValue(original.get(i), columnType.get(i)));
        }
        return copy;
    }

    public static boolean compare(Object left, Object right, ColumnType columnType, CompareOp op) {
        if (left == null || right == null) {
            return switch (op) {
                case EQ -> left == right;
                case NE -> left != right;
                default -> throw MysqlExecuteException.NULL_NOT_SUPPORTED_COMPARE();
            };
        }
        int cmp = columnType.compare(left, right);
        return switch (op) {
            case EQ -> cmp == 0;
            case NE -> cmp != 0;
            case GT -> cmp > 0;
            case GE -> cmp >= 0;
            case LT -> cmp < 0;
            case LE -> cmp <= 0;
        };
    }

    public static Row deepCopyProjectedRow(Row row, List<Integer> indices, List<Column> columns) {
        Object[] projected = new Object[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            int colIdx = indices.get(i);
            projected[i] = deepCopyValue(row.getValues()[colIdx], columns.get(colIdx).getColumnType());
        }
        Row out = new Row();
        out.setValues(projected);
        return out;
    }

    private static int compareOrdered(Object v1, Object v2, ColumnType columnType) {
        if (v1 == null && v2 == null) return 0;
        if (v1 == null) return -1;
        if (v2 == null) return 1;
        return Integer.compare(columnType.compare(v1, v2), 0);
    }

    public static int compareRowsOnTable(Row r1, Row r2, List<OrderByItem> orderByItems, List<Column> columns) {
        for (OrderByItem item : orderByItems) {
            int idx = columnName2Index(List.of(item.column()), columns).getFirst();
            ColumnType type = columns.get(idx).getColumnType();
            int cmp = compareOrdered(r1.getValues()[idx], r2.getValues()[idx], type);
            if (cmp != 0) {
                return item.ascending() ? cmp : -cmp;
            }
        }
        return 0;
    }
}
