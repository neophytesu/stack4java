package mysql.core.transaction.undo.dml;

import mysql.core.EngineContext;
import mysql.core.transaction.undo.UndoEntry;
import mysql.storage.Row;
import mysql.storage.Table;
import mysql.utils.MysqlUtil;

public record InsertUndo(String schemaName, String tableName, long oldNext, Row row) implements UndoEntry {
    @Override
    public void apply(EngineContext context) {
        context.useSchema(schemaName);
        context.useTable(tableName);
        Table table = context.getCurrentTable();
        MysqlUtil.removeByIdentity(table.getRows(), row);
        if (oldNext != -1) {
            table.setNextAutoIncrement(oldNext);
        }
    }
}
