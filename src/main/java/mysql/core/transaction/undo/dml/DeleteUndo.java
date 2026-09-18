package mysql.core.transaction.undo.dml;

import mysql.core.EngineContext;
import mysql.core.transaction.undo.UndoEntry;
import mysql.storage.Row;

public record DeleteUndo(String schemaName, String tableName, Row row) implements UndoEntry {
    @Override
    public void apply(EngineContext context) {
        context.useSchema(schemaName);
        context.useTable(tableName);
        context.getCurrentTable().getRows().add(row);
    }
}
