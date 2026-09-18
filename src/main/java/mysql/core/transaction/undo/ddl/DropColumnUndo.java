package mysql.core.transaction.undo.ddl;

import mysql.core.EngineContext;
import mysql.core.transaction.undo.UndoEntry;
import mysql.storage.Column;
import mysql.storage.Row;
import mysql.storage.Table;

import java.util.List;

public record DropColumnUndo(String schemaName, String tableName, int index, Column column, Integer oldPkIdx,
                             List<Row> rows, List<Object> cells) implements UndoEntry {

    @Override
    public void apply(EngineContext context) {
        context.useSchema(schemaName);
        context.useTable(tableName);
        Table table = context.getCurrentTable();
        table.getColumns().add(index, column);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            Object[] old = row.getValues();
            Object[] neu = new Object[old.length + 1];
            System.arraycopy(old, 0, neu, 0, index);
            neu[index] = cells.get(i);
            System.arraycopy(old, index, neu, index + 1, old.length - index);
            row.setValues(neu);
        }
        table.setPrimaryIdx(oldPkIdx);
    }
}


