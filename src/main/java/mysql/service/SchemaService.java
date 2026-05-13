package mysql.service;

import lombok.Data;
import mysql.base.ExecuteResult;
import mysql.storage.Schema;
import mysql.storage.Table;

import java.util.Map;

@Data
public class SchemaService {
    private Schema schema;

    public ExecuteResult useSchema(Schema schema) {
        this.schema = schema;
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult createTable(Table table) {
        Map<String, Table> tables = schema.getTables();
        String tableName = table.getTableName();
        if (tables.containsKey(tableName)) {
            return ExecuteResult.TABLE_EXIST();
        }
        tables.put(tableName, table);
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult dropTable(Table table) {
        Map<String, Table> tables = schema.getTables();
        String tableName = table.getTableName();
        if (!tables.containsKey(tableName)) {
            return ExecuteResult.TABLE_NOT_EXIST();
        }
        tables.remove(tableName);
        return ExecuteResult.SUCCESS();
    }
}
