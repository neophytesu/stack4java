package mysql.core.transaction.undo.ddl;

import mysql.core.EngineContext;
import mysql.core.transaction.undo.UndoEntry;
import mysql.storage.Table;

public record DropTableUndo(String schemaName, Table table) implements UndoEntry {
    @Override
    public void apply(EngineContext context) {
        context.useSchema(schemaName);
        context.getCurrentSchema().getTables().put(table.getTableName(), table);
    }
}
