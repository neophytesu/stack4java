package mysql.service;

import lombok.Data;
import mysql.base.ExecuteResult;
import mysql.core.EngineContext;
import mysql.core.transaction.undo.ddl.CreateTableUndo;
import mysql.core.transaction.undo.ddl.DropTableUndo;
import mysql.storage.Schema;
import mysql.storage.Table;

import java.util.Map;

@Data
public class SchemaService {
    private Schema schema;
    private EngineContext context;

    public void useSchema(Schema schema, EngineContext context) {
        this.schema = schema;
        this.context = context;
    }

    public ExecuteResult createTable(Table table) {
        Map<String, Table> tables = schema.getTables();
        String tableName = table.getTableName();
        if (tables.containsKey(tableName)) {
            return ExecuteResult.TABLE_EXIST();
        }
        tables.put(tableName, table);
        context.recordUndo(new CreateTableUndo(schema.getSchemaName(), tableName));
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult dropTable(String tableName) {
        Table existing = schema.getTables().get(tableName);
        if (existing == null) {
            return ExecuteResult.TABLE_NOT_EXIST();
        }
        schema.getTables().remove(tableName);
        context.recordUndo(new DropTableUndo(schema.getSchemaName(), existing));
        return ExecuteResult.SUCCESS();
    }
}
