package mysql.core.transaction.undo.ddl;

import mysql.core.EngineContext;
import mysql.core.transaction.undo.UndoEntry;
import mysql.storage.Schema;

public record CreateTableUndo(String schemaName, String tableName) implements UndoEntry {
    @Override
    public void apply(EngineContext context) {
        context.useSchema(schemaName);
        Schema schema = context.getCurrentSchema();
        schema.getTables().remove(tableName);
        if (context.getCurrentTable() != null && tableName.equals(context.getCurrentTable().getTableName())) {
            context.setCurrentTable(null);
            context.getTableService().useTable(null, context);
        }
    }
}
