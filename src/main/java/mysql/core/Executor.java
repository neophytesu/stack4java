package mysql.core;

import mysql.ast.statement.*;
import mysql.ast.statement.CreateSchemaStatement;
import mysql.ast.statement.CreateTableStatement;
import mysql.ast.statement.UseTableStatement;
import mysql.ast.statement.DeleteStatement;
import mysql.ast.statement.InsertStatement;
import mysql.ast.statement.UpdateStatement;
import mysql.ast.statement.UseSchemaStatement;
import mysql.ast.statement.SelectStatement;
import mysql.ast.statement.DefineStatement;
import mysql.ast.statement.ManipulateStatement;
import mysql.ast.statement.QueryStatement;
import mysql.base.ExecuteResult;
import mysql.base.MysqlExecuteException;
import mysql.storage.Schema;
import mysql.storage.Table;

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
                yield SqlResult.of(context.getTableService().select(s.columns(), s.where(), s.orderByItems(), s.limit(), s.offset()));
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
            case DropTableStatement s -> {
                ExecuteResult r = context.useSchema(s.schemaName());
                if (!r.isSuccess()) yield r;
                Table t = new Table();
                t.setTableName(s.tableName());
                ExecuteResult dropped = context.getSchemaService().dropTable(t);
                if (dropped.isSuccess() && context.getCurrentTable() != null && context.getCurrentTable().getTableName().equals(s.tableName())) {
                    context.setCurrentTable(null);
                    context.getTableService().useTable(null);
                }
                yield dropped;
            }
            case AddColumnStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().addColumn(s.column());
            }
            case DropColumnStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().dropColumn(s.columnName());
            }
        };
    }

    private ExecuteResult executeManipulate(ManipulateStatement stmt) {
        return switch (stmt) {
            case InsertStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().insert(s.columnNames(), s.rows());
            }
            case UpdateStatement s -> {
                ExecuteResult r = ensureSchemaAndTable(s.schemaName(), s.tableName());
                if (!r.isSuccess()) yield r;
                yield context.getTableService().update(s.assignments(), s.where());
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
