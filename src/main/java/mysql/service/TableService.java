package mysql.service;

import lombok.Data;
import mysql.base.ExecuteResult;
import mysql.base.MysqlExecuteException;
import mysql.storage.Column;
import mysql.storage.ColumnType;
import mysql.storage.Row;
import mysql.storage.Table;
import mysql.utils.MysqlUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class TableService {

    private Table table;

    public void useTable(Table table) {
        this.table = table;
    }

    public ExecuteResult addColumn(Column column) {
        List<Column> columns = table.getColumns();
        String columnName = column.getColumnName();
        if (columns.stream().anyMatch(c -> c.getColumnName().equals(columnName))) {
            return ExecuteResult.Column_EXIST();
        }
        columns.add(column);
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult dropColumn(Column column) {
        List<Column> columns = table.getColumns();
        String columnName = column.getColumnName();
        if (columns.stream().noneMatch(c -> c.getColumnName().equals(columnName))) {
            return ExecuteResult.Column_NOT_EXIST(column.getColumnName());
        }
        columns.removeIf(c -> c.getColumnName().equals(columnName));
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult insert(List<Object> values) {
        List<Column> columns = table.getColumns();
        List<ColumnType> columnTypes = columns.stream().map(Column::getColumnType).toList();
        if (columns.size() != values.size()) {
            return ExecuteResult.COLUMN_COUNT_MISMATCH();
        }
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (column.getColumnType().refuse(values.get(i))) {
                return ExecuteResult.COLUMN_TYPE_MISMATCH(column.getColumnName());
            }
        }
        Row row = new Row();
        row.setValues(MysqlUtil.deepCopy(values, columnTypes).toArray());
        table.getRows().add(row);
        return ExecuteResult.INSERT_SUCCESS(1);
    }

    public List<Row> selectAll() {
        return Collections.unmodifiableList(table.getRows());
    }

    public List<Row> selectColumns(List<String> columnNames) {
        List<Integer> columnIndices = MysqlUtil.columnName2Index(columnNames, table.getColumns());
        int len = columnIndices.size();
        List<Row> result = new ArrayList<>();
        for (Row row : table.getRows()) {
            Object[] selectData = new Object[len];
            for (int i = 0; i < len; i++) {
                selectData[i] = row.getValues()[columnIndices.get(i)];
            }
            Row selectRow = new Row();
            selectRow.setValues(selectData);
            result.add(selectRow);
        }
        return Collections.unmodifiableList(result);
    }

    public List<Row> selectWhere(String columnName, Object value) {
        int idx = -1;
        List<Column> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (columnName.equals(columns.get(i).getColumnName())) {
                idx = i;
                break;
            }
        }
        if (idx == -1) {
            throw new MysqlExecuteException(102L, "列" + columnName + "不存在");
        }
        List<Row> res = new ArrayList<>();
        ColumnType columnType = columns.get(idx).getColumnType();
        if (columnType.refuse(value)) {
            throw new MysqlExecuteException(103L, "列" + columnName + "的类型和插入值" + value + "不符");
        }
        for (Row row : table.getRows()) {
            if (MysqlUtil.compareRowValue(row.getValues()[idx], value, columnType)) {
                res.add(MysqlUtil.copyRow(row));
            }
        }
        return Collections.unmodifiableList(res);
    }

    public ExecuteResult deleteAll() {
        int num = table.getRows().size();
        table.getRows().clear();
        return ExecuteResult.DELETE_SUCCESS(num);
    }

    public ExecuteResult deleteByPrimaryKey(Object primaryValue) {
        int primaryKeyIdx = table.getPrimaryIdx();
        ColumnType columnType = table.getColumns().get(primaryKeyIdx).getColumnType();
        List<Row> rows = table.getRows();
        for (int i = 0; i < table.getRows().size(); i++) {
            if (MysqlUtil.compareRowValue(primaryValue, rows.get(i).getValues()[primaryKeyIdx], columnType)) {
                rows.remove(i);
                return ExecuteResult.DELETE_SUCCESS(1);
            }
        }
        return ExecuteResult.DELETE_SUCCESS(0);
    }

    public ExecuteResult updateAll(Integer columnIdx, Object newValue) {
        if (columnIdx < 0 || columnIdx >= table.getColumns().size()) {
            throw new MysqlExecuteException(104L, "列索引越界");
        }
        Column column = table.getColumns().get(columnIdx);
        if (column.getColumnType().refuse(newValue)) {
            return ExecuteResult.COLUMN_TYPE_MISMATCH(column.getColumnName());
        }
        for (Row row : table.getRows()) {
            row.getValues()[columnIdx] = MysqlUtil.deepCopyValue(newValue, column.getColumnType());
        }
        return ExecuteResult.UPDATE_SUCCESS(table.getRows().size());
    }

    public ExecuteResult updateByPrimaryKey(Object pkValue, int columnIdx, Object newValue) {
        if (columnIdx < 0 || columnIdx >= table.getColumns().size()) {
            throw new MysqlExecuteException(104L, "列索引越界");
        }
        int pkIdx = table.getPrimaryIdx();
        Column pkColumn = table.getColumns().get(pkIdx);
        ColumnType pkColumnType = pkColumn.getColumnType();
        Column column = table.getColumns().get(columnIdx);
        ColumnType columnType = column.getColumnType();
        if (pkColumnType.refuse(pkValue)) {
            throw new MysqlExecuteException(103L, "列" + pkColumn.getColumnName() + "的类型和检索值" + pkValue + "不符");
        }
        if (columnType.refuse(newValue)) {
            throw new MysqlExecuteException(103L, "列" + column.getColumnName() + "的类型和插入值" + newValue + "不符");
        }
        for (Row row : table.getRows()) {
            if (MysqlUtil.compareRowValue(pkValue, row.getValues()[pkIdx], pkColumnType)) {
                row.getValues()[columnIdx] = MysqlUtil.deepCopyValue(newValue, columnType);
                return ExecuteResult.UPDATE_SUCCESS(1);
            }
        }
        return ExecuteResult.UPDATE_SUCCESS(0);
    }
}
