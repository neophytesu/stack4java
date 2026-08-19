package mysql.core;

import mysql.ast.*;
import mysql.base.ExecuteResult;
import mysql.storage.Schema;

public class Executor {
    private final EngineContext context;

    public Executor(EngineContext context) {
        this.context = context;
    }


    public Object execute(Statement stmt) {
        return switch (stmt) {
            case UseSchemaStatement s -> context.useSchema(s.schemaName());
            case UseTableStatement s -> context.useTable(s.tableName());
            case CreateSchemaStatement s -> {
                Schema schema = new Schema();
                schema.setSchemaName(s.schemaName());
                yield context.getCatalogService().createSchema(schema);
            }
            case CreateTableStatement s -> {
                ExecuteResult r = context.useSchema(s.schemaName());
                if (!r.isSuccess()) yield r;
                yield context.getSchemaService().createTable(s.table());
            }
            case InsertStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().insert(s.value());
            }
            case SelectWhereStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().selectWhere(s.columnName(), s.value());
            }
        };
    }

    private ExecuteResult ensureSchemaAndTable(String schemaName, String tableName) {
        ExecuteResult r = context.useSchema(schemaName);
        if (!r.isSuccess()) return r;
        return context.useTable(tableName);
    }
}
