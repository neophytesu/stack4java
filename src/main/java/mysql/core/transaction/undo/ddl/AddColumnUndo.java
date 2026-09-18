package mysql.core.transaction.undo.ddl;

import mysql.core.EngineContext;
import mysql.core.transaction.undo.UndoEntry;
import mysql.storage.Column;
import mysql.storage.Table;
import mysql.utils.MysqlUtil;

import java.util.List;

public record AddColumnUndo(String schemaName, String tableName, String columnName, int index) implements UndoEntry {
    @Override
    public void apply(EngineContext context) {
        context.useSchema(schemaName);
        context.useTable(tableName);
        Table table = context.getCurrentTable();
        List<Column> columns = table.getColumns();
        columns.remove(index);
        MysqlUtil.delColumn(table.getRows(), index);
    }
}
