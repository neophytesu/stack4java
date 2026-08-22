package mysql.service;

import lombok.Data;
import mysql.ast.expr.Expr;
import mysql.ast.expr.ExprEvaluator;
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

    public ExecuteResult deleteAll() {
        int num = table.getRows().size();
        table.getRows().clear();
        return ExecuteResult.DELETE_SUCCESS(num);
    }

    public ExecuteResult deleteWhere(Expr where) {
        List<Row> rows = table.getRows();
        int count = 0;
        for (int i = rows.size() - 1; i >= 0; i--) {
            if (ExprEvaluator.eval(where, rows.get(i), table)) {
                rows.remove(i);
                count++;
            }
        }
        return ExecuteResult.DELETE_SUCCESS(count);
    }

    public ExecuteResult updateAll(Integer columnIdx, Object newValue) {
        if (columnIdx < 0 || columnIdx >= table.getColumns().size()) {
            throw MysqlExecuteException.COLUMN_INDEX_OVER();
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

    public ExecuteResult updateWhere(Expr where, int columnIdx, Object newValue) {
        if (columnIdx < 0 || columnIdx >= table.getColumns().size()) {
            throw MysqlExecuteException.COLUMN_INDEX_OVER();
        }
        Column column = table.getColumns().get(columnIdx);
        ColumnType columnType = column.getColumnType();
        if (columnType.refuse(newValue)) {
            throw MysqlExecuteException.COLUMN_TYPE_NOT_MATCHED(column.getColumnName(), newValue);
        }
        int count = 0;
        for (Row row : table.getRows()) {
            if (ExprEvaluator.eval(where, row, table)) {
                row.getValues()[columnIdx] = MysqlUtil.deepCopyValue(newValue, columnType);
                count++;
            }
        }
        return ExecuteResult.UPDATE_SUCCESS(count);
    }

    public List<Row> selectWhere(Expr where) {
        List<Row> res = new ArrayList<>();
        for (Row row : table.getRows()) {
            if (ExprEvaluator.eval(where, row, table)) {
                res.add(MysqlUtil.copyRow(row));
            }
        }
        return Collections.unmodifiableList(res);
    }
}
