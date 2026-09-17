package mysql.core.transaction;

import mysql.core.EngineContext;
import mysql.storage.Row;
import mysql.storage.Schema;
import mysql.storage.Table;

public record DeleteUndo(Schema schema, Table table, Row row) implements UndoEntry {
    @Override
    public void apply(EngineContext context) {
        context.useSchema(schema.getSchemaName());
        context.useTable(table.getTableName());
        table.getRows().add(row);
    }
}
