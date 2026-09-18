package mysql.core.transaction.undo.dml;

import mysql.core.EngineContext;
import mysql.core.transaction.undo.UndoEntry;
import mysql.storage.Row;

import java.util.List;

public record UpdateUndo(String schemaName, String tableName, Row row, List<Object> oldValues,
                         long oldNext) implements UndoEntry {

    @Override
    public void apply(EngineContext context) {
        context.useSchema(schemaName);
        context.useTable(tableName);
        row.setValues(oldValues.toArray());
        if (oldNext != -1) {
            context.getCurrentTable().setNextAutoIncrement(oldNext);
        }
    }
}
