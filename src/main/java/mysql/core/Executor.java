package mysql.core;

import mysql.ast.statement.*;
import mysql.base.ExecuteResult;
import mysql.storage.Schema;
import mysql.utils.MysqlUtil;

import java.util.List;

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
            case SelectAllStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().selectAll();
            }
            case SelectColumnsStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().selectColumns(s.columnNames());
            }
            case UpdateByPrimaryKeyStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                int columnIdx = MysqlUtil.columnName2Index(List.of(s.columnName()), context.getCurrentTable().getColumns()).getFirst();
                yield context.getTableService().updateByPrimaryKey(s.pkValue(), columnIdx, s.newValue());
            }
            case DeleteAllStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().deleteAll();
            }
            case DeleteByPrimaryKeyStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().deleteByPrimaryKey(s.pkValue());
            }
        };
    }

    private ExecuteResult ensureSchemaAndTable(String schemaName, String tableName) {
        ExecuteResult r = context.useSchema(schemaName);
        if (!r.isSuccess()) return r;
        return context.useTable(tableName);
    }
}
