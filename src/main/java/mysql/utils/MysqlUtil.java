package mysql.utils;

import mysql.ast.expr.CompareOp;
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
}
