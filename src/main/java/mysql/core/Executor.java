package mysql.core;

import mysql.ast.statement.*;
import mysql.base.ExecuteResult;
import mysql.base.MysqlExecuteException;
import mysql.storage.Schema;

public class Executor {
    private final EngineContext context;

    public Executor(EngineContext context) {
        this.context = context;
    }

    public SqlResult execute(Statement stmt) {
        try {
            return switch (stmt) {
                case QueryStatement queryStmt -> executeQuery(queryStmt);
                case ManipulateStatement manipulateStmt -> SqlResult.of(executeManipulate(manipulateStmt));
                case DefineStatement defineStmt -> SqlResult.of(executeDefine(defineStmt));
            };
        } catch (MysqlExecuteException e) {
            return SqlResult.of(ExecuteResult.convertException(e));
        }
    }

    private SqlResult executeQuery(QueryStatement stmt) {
        return switch (stmt) {
            case SelectStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) {
                    yield SqlResult.of(r);
                }
                yield SqlResult.of(context.getTableService().select(s.columns(), s.where()));
            }
        };
    }

    private ExecuteResult executeDefine(DefineStatement stmt) {
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

    private ExecuteResult executeManipulate(ManipulateStatement stmt) {
        return switch (stmt) {
            case InsertStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().insert(s.values());
            }
            case UpdateStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().update(s.columnName(), s.newValue(), s.where());
            }
            case DeleteStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().delete(s.where());
            }
        };
    }

    private ExecuteResult ensureSchemaAndTable(String schemaName, String tableName) {
        ExecuteResult r = context.useSchema(schemaName);
        if (!r.isSuccess()) return r;
        return context.useTable(tableName);
    }

}
