package mysql.service;

import lombok.Data;
import mysql.ast.expr.Expr;
import mysql.ast.expr.ExprEvaluator;
import mysql.ast.statement.Assignment;
import mysql.ast.statement.OrderByItem;
import mysql.base.ExecuteResult;
import mysql.storage.Column;
import mysql.storage.ColumnType;
import mysql.storage.Row;
import mysql.storage.Table;
import mysql.utils.MysqlUtil;

import java.util.*;

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

    public ExecuteResult insert(List<String> columnNames, List<List<Object>> rows) {
        List<Row> pending = new ArrayList<>(rows.size());
        long next = table.getNextAutoIncrement();
        for (List<Object> values : rows) {
            Object[] rowData = new Object[table.getColumns().size()];
            ExecuteResult r = buildRowData(columnNames, values, rowData);
            if (!r.isSuccess()) {
                return r;
            }
            next = fillAutoIncrement(rowData, next);
            Row row = new Row();
            row.setValues(rowData);
            pending.add(row);
        }
        ExecuteResult pkCheck = checkPrimaryKeysForBatch(pending);
        if (!pkCheck.isSuccess()) {
            return pkCheck;
        }
        table.getRows().addAll(pending);
        table.setNextAutoIncrement(next);
        return ExecuteResult.INSERT_SUCCESS(pending.size());
    }

    private ExecuteResult checkPrimaryKeysForBatch(List<Row> pending) {
        Integer pkIdx = table.getPrimaryIdx();
        if (pkIdx == null) {
            return ExecuteResult.SUCCESS();
        }
        Set<Object> batchPks = new HashSet<>();
        for (Row row : pending) {
            Object pk = row.getValues()[pkIdx];
            if (pk == null) {
                return ExecuteResult.PRIMARY_KEY_IS_NULL();
            }
            if (!batchPks.add(pk)) {
                return ExecuteResult.PRIMARY_KEY_REPEATED();
            }
            for (Row existing : table.getRows()) {
                if (Objects.equals(pk, existing.getValues()[pkIdx])) {
                    return ExecuteResult.PRIMARY_KEY_REPEATED();
                }
            }
        }
        return ExecuteResult.SUCCESS();
    }

    private ExecuteResult buildRowData(List<String> columnNames, List<Object> values, Object[] rowData) {
        List<Column> columns = table.getColumns();
        List<ColumnType> allTypes = columns.stream().map(Column::getColumnType).toList();
        if (columnNames == null) {
            if (columns.size() != values.size()) {
                return ExecuteResult.COLUMN_COUNT_MISMATCH();
            }
            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                if (column.getColumnType().refuse(values.get(i))) {
                    return ExecuteResult.COLUMN_TYPE_MISMATCH(column.getColumnName());
                }
            }
            Object[] copied = MysqlUtil.deepCopy(values, allTypes).toArray();
            System.arraycopy(copied, 0, rowData, 0, copied.length);
        } else {
            if (columnNames.size() != values.size()) {
                return ExecuteResult.COLUMN_COUNT_MISMATCH();
            }
            for (int i = 0; i < columns.size(); i++) {
                rowData[i] = null;
            }
            for (int i = 0; i < columnNames.size(); i++) {
                int idx = MysqlUtil.columnName2Index(List.of(columnNames.get(i)), columns).getFirst();
                Column column = columns.get(idx);
                Object value = values.get(i);
                if (column.getColumnType().refuse(value)) {
                    return ExecuteResult.COLUMN_TYPE_MISMATCH(column.getColumnName());
                }
                rowData[idx] = MysqlUtil.deepCopyValue(value, column.getColumnType());
            }
        }
        return ExecuteResult.SUCCESS();
    }

    private long fillAutoIncrement(Object[] rowData, long next) {
        Integer pkIdx = table.getPrimaryIdx();
        Column pkColumn = table.getColumns().get(pkIdx);
        if (!pkColumn.isAutoIncrement()) {
            return next;
        }
        Object pk = rowData[pkIdx];
        if (pk != null) {
            return bumpNext(next, pk);
        }
        rowData[pkIdx] = (int) next;
        return next + 1;
    }

    private ExecuteResult checkPrimaryKey(Row row, Row exclude) {
        Integer pkIdx = table.getPrimaryIdx();
        if (pkIdx == null) {
            return ExecuteResult.SUCCESS();
        }
        Object pk = row.getValues()[pkIdx];
        if (pk == null) {
            return ExecuteResult.PRIMARY_KEY_IS_NULL();
        }
        for (Row existing : table.getRows()) {
            if (existing == exclude) {
                continue;
            }
            if (Objects.equals(pk, existing.getValues()[pkIdx])) {
                return ExecuteResult.PRIMARY_KEY_REPEATED();
            }
        }
        return ExecuteResult.SUCCESS();
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

    public ExecuteResult update(List<Assignment> assignments, Expr where) {
        Integer pkIdx = table.getPrimaryIdx();
        String pkName = pkIdx != null ? table.getColumns().get(pkIdx).getColumnName() : null;
        boolean touchesPk = pkName != null && assignments.stream().anyMatch(c -> c.columnName().equals(pkName));
        int count = 0;
        for (Row row : table.getRows()) {
            if (where != null && !ExprEvaluator.eval(where, row, table)) {
                continue;
            }
            for (Assignment assignment : assignments) {
                int columnIdx = MysqlUtil.columnName2Index(List.of(assignment.columnName()), table.getColumns()).getFirst();
                Column column = table.getColumns().get(columnIdx);
                ColumnType columnType = column.getColumnType();
                if (columnType.refuse(assignment.newValue())) {
                    return ExecuteResult.COLUMN_TYPE_MISMATCH(column.getColumnName());
                }
                row.getValues()[columnIdx] = MysqlUtil.deepCopyValue(assignment.newValue(), columnType);
            }
            if (touchesPk) {
                ExecuteResult pkCheck = checkPrimaryKey(row, row);
                if (!pkCheck.isSuccess()) {
                    return pkCheck;
                }
                long updated = bumpNext(table.getNextAutoIncrement(), row.getValues()[pkIdx]);
                table.setNextAutoIncrement(updated);
            }
            count++;
        }
        return ExecuteResult.UPDATE_SUCCESS(count);
    }

    private long bumpNext(long next, Object pkValue) {
        if (pkValue == null) {
            return next;
        }
        long explicit = ((Integer) pkValue).longValue();
        return explicit >= next ? explicit + 1 : next;
    }

    public List<Row> select(List<String> columnNames, Expr where, List<OrderByItem> orderByItems, Integer limit, Integer offset) {
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
        List<Row> matched = new ArrayList<>();
        for (Row row : table.getRows()) {
            if (where == null || ExprEvaluator.eval(where, row, table)) {
                matched.add(row);
            }
        }
        if (orderByItems != null) {
            matched.sort((r1, r2) -> MysqlUtil.compareRowsOnTable(r1, r2, orderByItems, columns));
        }
        if (limit != null) {
            int from = offset == null ? 0 : offset;
            int to = Math.min(from + limit, matched.size());
            if (from >= matched.size()) {
                matched = List.of();
            } else {
                matched = new ArrayList<>(matched.subList(from, to));
            }
        }
        List<Row> projected = new ArrayList<>(matched.size());
        for (Row row : matched) {
            projected.add(MysqlUtil.deepCopyProjectedRow(row, indices, columns));
        }
        return Collections.unmodifiableList(projected);
    }
}
