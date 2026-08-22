package mysql.core;

import mysql.ast.statement.*;
import mysql.base.ExecuteResult;
import mysql.base.MysqlExecuteException;
import mysql.storage.Row;
import mysql.storage.Schema;
import mysql.utils.MysqlUtil;

import java.util.List;

public class Executor {
    private final EngineContext context;

    public Executor(EngineContext context) {
        this.context = context;
    }

    public List<Row> executeQuery(QueryStatement stmt) {
        return switch (stmt) {
            case SelectWhereStatement s -> {
                requireSchemaAndTable(s.schemaName(), s.tableName());
                yield context.getTableService().selectWhere(s.where());
            }
            case SelectAllStatement s -> {
                requireSchemaAndTable(s.schemaName(), s.tableName());
                yield context.getTableService().selectAll();
            }
            case SelectColumnsStatement s -> {
                requireSchemaAndTable(s.schemaName(), s.tableName());
                yield context.getTableService().selectColumns(s.columnNames());
            }
        };
    }

    public ExecuteResult executeDefine(DefineStatement stmt) {
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
        };
    }

    public ExecuteResult executeUpdate(UpdateStatement stmt) {
        return switch (stmt) {
            case InsertStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().insert(s.values());
            }
            case UpdateWhereStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                int columnIdx = MysqlUtil.columnName2Index(List.of(s.columnName()), context.getCurrentTable().getColumns()).getFirst();
                yield context.getTableService().updateWhere(s.where(), columnIdx, s.newValue());
            }
            case DeleteAllStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().deleteAll();
            }
            case DeleteWhereStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().deleteWhere(s.where());
            }
        };
    }

    private ExecuteResult ensureSchemaAndTable(String schemaName, String tableName) {
        ExecuteResult r = context.useSchema(schemaName);
        if (!r.isSuccess()) return r;
        return context.useTable(tableName);
    }

    private void requireSchemaAndTable(String schemaName, String tableName) {
        ExecuteResult r = ensureSchemaAndTable(schemaName, tableName);
        if (!r.isSuccess()) {
            throw new MysqlExecuteException(r.code(), r.description());
        }
    }

}
