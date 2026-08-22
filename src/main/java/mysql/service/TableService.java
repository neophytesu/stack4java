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

    public List<Row> select(List<String> columnNames, Expr where) {
        List<Column> columns = table.getColumns();
        List<Integer> indices;
        if (columnNames == null) {
            indices = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                indices.add(i);
            }
        } else {
            indices = MysqlUtil.columnName2Index(columnNames, columns);
        }
        List<Row> result = new ArrayList<>();
        for (Row row : table.getRows()) {
            if (where != null && !ExprEvaluator.eval(where, row, table)) {
                continue;
            }
            Object[] projected = new Object[indices.size()];
            for (int i = 0; i < indices.size(); i++) {
                projected[i] = row.getValues()[indices.get(i)];
            }
            Row out = new Row();
            out.setValues(projected);
            result.add(out);
        }
        return Collections.unmodifiableList(result);
    }
}
