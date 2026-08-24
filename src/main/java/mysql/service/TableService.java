package mysql.service;

import lombok.Data;
import mysql.ast.expr.Expr;
import mysql.ast.expr.ExprEvaluator;
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

    public ExecuteResult delete(Expr where) {
        List<Row> rows = table.getRows();
        if (where == null) {
            int num = rows.size();
            rows.clear();
            return ExecuteResult.DELETE_SUCCESS(num);
        }
        int count = 0;
        for (int i = rows.size() - 1; i >= 0; i--) {
            if (ExprEvaluator.eval(where, rows.get(i), table)) {
                rows.remove(i);
                count++;
            }
        }
        return ExecuteResult.DELETE_SUCCESS(count);
    }

    public ExecuteResult update(String columnName, Object newValue, Expr where) {
        int columnIdx = MysqlUtil.columnName2Index(List.of(columnName), table.getColumns()).getFirst();
        Column column = table.getColumns().get(columnIdx);
        ColumnType columnType = column.getColumnType();
        if (columnType.refuse(newValue)) {
            return ExecuteResult.COLUMN_TYPE_MISMATCH(column.getColumnName());
        }
        int count = 0;
        for (Row row : table.getRows()) {
            if (where != null && !ExprEvaluator.eval(where, row, table)) {
                continue;
            }
            row.getValues()[columnIdx] = MysqlUtil.deepCopyValue(newValue, columnType);
            count++;
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
            result.add(MysqlUtil.deepCopyProjectedRow(row, indices, columns));
        }
        return Collections.unmodifiableList(result);
    }
}
