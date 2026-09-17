package mysql.core.transaction;

import mysql.core.EngineContext;
import mysql.storage.Row;
import mysql.storage.Schema;
import mysql.storage.Table;
import mysql.utils.MysqlUtil;

public record InsertUndo(Schema schema, Table table, long oldNext, Row row) implements UndoEntry {
    @Override
    public void apply(EngineContext context) {
        context.useSchema(schema.getSchemaName());
        context.useTable(table.getTableName());
        MysqlUtil.removeByIdentity(table.getRows(),row);
        if (oldNext != -1) {
            table.setNextAutoIncrement(oldNext);
        }
    }
}
