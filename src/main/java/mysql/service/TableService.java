package mysql.service;

import lombok.Data;
import mysql.ast.expr.compare.Expr;
import mysql.ast.expr.compare.ExprEvaluator;
import mysql.ast.expr.value.ValueExprEvaluator;
import mysql.ast.statement.Assignment;
import mysql.ast.statement.OrderByItem;
import mysql.base.ExecuteResult;
import mysql.base.MysqlExecuteException;
import mysql.core.EngineContext;
import mysql.core.transaction.undo.ddl.AddColumnUndo;
import mysql.core.transaction.undo.ddl.DropColumnUndo;
import mysql.core.transaction.undo.dml.DeleteUndo;
import mysql.core.transaction.undo.dml.InsertUndo;
import mysql.core.transaction.undo.dml.UpdateUndo;
import mysql.storage.Column;
import mysql.storage.ColumnType;
import mysql.storage.Row;
import mysql.storage.Table;
import mysql.utils.MysqlUtil;

import java.util.*;

@Data
public class TableService {

    private Table table;
    private EngineContext context;

    public void useTable(Table table, EngineContext context) {
        this.table = table;
        this.context = context;
    }

    public ExecuteResult addColumn(Column column) {
        List<Column> columns = table.getColumns();
        String columnName = column.getColumnName();
        if (columns.stream().anyMatch(c -> c.getColumnName().equals(columnName))) {
            return ExecuteResult.Column_EXIST();
        }
        columns.add(column);
        for (Row row : table.getRows()) {
            Object[] old = row.getValues();
            Object[] neu = Arrays.copyOf(old, old.length + 1);
            neu[old.length] = null;
            row.setValues(neu);
        }
        int idx = -1;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getColumnName().equals(columnName)) {
                idx = i;
                break;
            }
        }
        context.recordUndo(new AddColumnUndo(context.getCurrentSchema().getSchemaName(), table.getTableName(), columnName, idx));
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult dropColumn(String columnName) {
        List<Column> columns = table.getColumns();
        int idx = -1;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getColumnName().equals(columnName)) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            return ExecuteResult.Column_NOT_EXIST(columnName);
        }
        Integer pkIdx = table.getPrimaryIdx();
        if (pkIdx != null && pkIdx == idx) {
            return ExecuteResult.PRIMARY_KEY_DROP_NOT_ALLOWED();
        }
        Column dropped = columns.get(idx);
        ArrayList<Row> snapshotRows = new ArrayList<>(table.getRows());
        ArrayList<Object> cells = new ArrayList<>(snapshotRows.size());
        ColumnType type = dropped.getColumnType();
        for (Row row : snapshotRows) {
            cells.add(MysqlUtil.deepCopyValue(row.getValues()[idx], type));
        }
        columns.remove(idx);
        context.recordUndo(new DropColumnUndo(context.getCurrentSchema().getSchemaName(), table.getTableName(), idx, dropped, pkIdx, snapshotRows, cells));
        MysqlUtil.delColumn(table.getRows(), idx);
        if (pkIdx != null && pkIdx > idx) {
            table.setPrimaryIdx(pkIdx - 1);
        }
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult insert(List<String> columnNames, List<List<Object>> rows) {
        List<Row> pending = new ArrayList<>(rows.size());
        long oldNext = -1;
        Integer pkIdx = table.getPrimaryIdx();
        if (pkIdx != null && table.getColumns().get(pkIdx).isAutoIncrement()) {
            oldNext = table.getNextAutoIncrement();
        }
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
        for (Row row : pending) {
            context.recordUndo(new InsertUndo(context.getCurrentSchema().getSchemaName(), table.getTableName(), oldNext, row));
        }
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
        if (pkIdx == null) {
            return next;
        }
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

    public ExecuteResult delete(Expr where) {
        List<Row> rows = table.getRows();
        if (where == null) {
            for (Row row : rows) {
                context.recordUndo(new DeleteUndo(context.getCurrentSchema().getSchemaName(), table.getTableName(), row));
            }
            int num = rows.size();
            rows.clear();
            return ExecuteResult.DELETE_SUCCESS(num);
        }
        int count = 0;
        for (int i = rows.size() - 1; i >= 0; i--) {
            Row row = rows.get(i);
            if (ExprEvaluator.eval(where, row, table)) {
                context.recordUndo(new DeleteUndo(context.getCurrentSchema().getSchemaName(), table.getTableName(), row));
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
        List<ColumnType> columnTypes = table.getColumns().stream().map(Column::getColumnType).toList();
        long oldNext = touchesPk ? table.getNextAutoIncrement() : -1;
        List<PendingUpdate> pending = new ArrayList<>();
        for (Row row : table.getRows()) {
            if (where != null && !ExprEvaluator.eval(where, row, table)) {
                continue;
            }
            List<Object> oldValues = MysqlUtil.deepCopy(Arrays.asList(row.getValues()), columnTypes);
            Object[] newValues = Arrays.copyOf(row.getValues(), row.getValues().length);
            Row scratch = new Row();
            scratch.setValues(newValues);
            for (Assignment assignment : assignments) {
                int columnIdx = MysqlUtil.columnName2Index(List.of(assignment.columnName()), table.getColumns()).getFirst();
                Column column = table.getColumns().get(columnIdx);
                ColumnType columnType = column.getColumnType();
                Object computed;
                try {
                    computed = ValueExprEvaluator.eval(assignment.value(), scratch, table);
                } catch (MysqlExecuteException e) {
                    return ExecuteResult.convertException(e);
                }
                if (columnType.refuse(computed)) {
                    return ExecuteResult.COLUMN_TYPE_MISMATCH(column.getColumnName());
                }
                newValues[columnIdx] = MysqlUtil.deepCopyValue(computed, columnType);
            }
            pending.add(new PendingUpdate(row, oldValues, newValues, oldNext));
        }
        if (touchesPk) {
            ExecuteResult pkCheck = checkPendingPrimaryKeys(pending, pkIdx);
            if (!pkCheck.isSuccess()) {
                return pkCheck;
            }
        }
        long next = table.getNextAutoIncrement();
        for (PendingUpdate p : pending) {
            p.row.setValues(p.newValues);
            if (touchesPk) {
                next = bumpNext(next, p.newValues[pkIdx]);
            }
            context.recordUndo(new UpdateUndo(context.getCurrentSchema().getSchemaName(), table.getTableName(), p.row, p.oldValues, p.oldNext));
        }
        if (touchesPk) {
            table.setNextAutoIncrement(next);
        }
        return ExecuteResult.UPDATE_SUCCESS(pending.size());
    }

    private ExecuteResult checkPendingPrimaryKeys(List<PendingUpdate> pending, Integer pkIdx) {
        Set<Object> newPks = new HashSet<>();
        Set<Row> changing = Collections.newSetFromMap(new IdentityHashMap<>());
        for (PendingUpdate p : pending) {
            changing.add(p.row);
            Object pk = p.newValues[pkIdx];
            if (pk == null) {
                return ExecuteResult.PRIMARY_KEY_IS_NULL();
            }
            if (!newPks.add(pk)) {
                return ExecuteResult.PRIMARY_KEY_REPEATED();
            }
        }
        for (Row existing : table.getRows()) {
            if (changing.contains(existing)) {
                continue;
            }
            if (newPks.contains(existing.getValues()[pkIdx])) {
                return ExecuteResult.PRIMARY_KEY_REPEATED();
            }
        }
        return ExecuteResult.SUCCESS();
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

    record PendingUpdate(Row row, List<Object> oldValues, Object[] newValues, long oldNext) {
    }
}
