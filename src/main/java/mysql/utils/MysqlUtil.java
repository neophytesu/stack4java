package mysql.utils;

import mysql.base.MysqlExecuteException;
import mysql.storage.Column;
import mysql.storage.ColumnType;
import mysql.storage.Row;

import java.util.*;

public class MysqlUtil {
    public static boolean compareRowValue(Object a, Object b, ColumnType columnType) {
        if (a == null || b == null) {
            return a == b;
        }
        return columnType.equalsValue(a, b);
    }

    public static Object deepCopyValue(Object value, ColumnType columnType) {
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
                throw new MysqlExecuteException(101L, "没找到列名为" + columnName + "的列");
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

    public static Row copyRow(Row row) {
        int len = row.getValues().length;
        Object[] data = new Object[len];
        System.arraycopy(row.getValues(), 0, data, 0, len);
        Row newRow = new Row();
        newRow.setValues(data);
        return newRow;
    }

}
