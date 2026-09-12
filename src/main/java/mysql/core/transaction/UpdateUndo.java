package mysql.core.transaction;

import mysql.core.EngineContext;
import mysql.storage.Row;
import mysql.storage.Schema;
import mysql.storage.Table;

import java.util.List;

public record UpdateUndo(Schema schema, Table table, Row row, List<Object> oldValues,
                         long oldNext) implements UndoEntry {

    @Override
    public void apply(EngineContext context) {
        context.useSchema(schema.getSchemaName());
        context.useTable(table.getTableName());
        row.setValues(oldValues.toArray());
        if (oldNext != -1) {
            table.setNextAutoIncrement(oldNext);
        }
    }
}
