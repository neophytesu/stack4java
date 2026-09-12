package mysql.core.transaction;

import mysql.core.EngineContext;
import mysql.storage.Row;
import mysql.storage.Schema;
import mysql.storage.Table;

import java.util.List;

public record DeleteUndo(Schema schema, Table table, List<Object> oldValue) implements UndoEntry {
    @Override
    public void apply(EngineContext context) {
        context.useSchema(schema.getSchemaName());
        context.useTable(table.getTableName());
        Row row = new Row();
        row.setValues(oldValue.toArray());
        table.getRows().add(row);
    }
}
