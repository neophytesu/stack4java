package mysql.core.transaction.undo.ddl;

import mysql.core.EngineContext;
import mysql.core.transaction.undo.UndoEntry;
import mysql.storage.Column;
import mysql.storage.Row;
import mysql.storage.Table;

import java.util.Arrays;
import java.util.List;

public record AddColumnUndo(String schemaName, String tableName, String columnName, int idx) implements UndoEntry {
    @Override
    public void apply(EngineContext context) {
        context.useSchema(schemaName);
        context.useTable(tableName);
        Table table = context.getCurrentTable();
        List<Column> columns = table.getColumns();
        columns.remove(idx);
        for (Row row : table.getRows()) {
            Object[] old = row.getValues();
            row.setValues(Arrays.copyOf(old, old.length - 1));
        }
    }
}
