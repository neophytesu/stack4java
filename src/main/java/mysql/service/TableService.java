package mysql.service;

import lombok.Data;
import mysql.base.ExecuteResult;
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

    public ExecuteResult useTable(Table table) {
        this.table = table;
        return ExecuteResult.SUCCESS();
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
            return ExecuteResult.Column_NOT_EXIST();
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
        return ExecuteResult.SUCCESS();
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

    public ExecuteResult deleteAll() {
        table.getRows().clear();
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult deleteByPrimaryKey(Object primaryValue) {
        int primaryKeyIdx = table.getPrimaryIdx();
        ColumnType columnType = table.getColumns().get(primaryKeyIdx).getColumnType();
        List<Row> rows = table.getRows();
        for (int i = 0; i < table.getRows().size(); i++) {
            if (MysqlUtil.compareRowValue(primaryValue, rows.get(i).getValues()[primaryKeyIdx], columnType)) {
                rows.remove(i);
                break;
            }
        }
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult updateAll(Integer columnIdx, Object newValue) {
        Column column = table.getColumns().get(columnIdx);
        if (column.getColumnType().refuse(newValue)) {
            return ExecuteResult.COLUMN_TYPE_MISMATCH(column.getColumnName());
        }
        for (Row row : table.getRows()) {
            row.getValues()[columnIdx] = newValue;
        }
        return ExecuteResult.SUCCESS();
    }
}
