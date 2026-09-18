package mysql.core;

import lombok.Getter;
import mysql.ast.parser.ParsedSql;
import mysql.ast.parser.SqlParseException;
import mysql.ast.parser.SqlParser;
import mysql.ast.statement.*;
import mysql.base.ExecuteResult;
import mysql.core.transaction.undo.UndoTransactionManager;
import mysql.core.transaction.TransactionManager;
import mysql.storage.Catalog;

public class SqlEngine {
    @Getter
    private final EngineContext context;
    private final SqlParser parser;
    private final Executor executor;
    private final TransactionManager transactionManager = new UndoTransactionManager();

    public SqlEngine(Catalog catalog) {
        this.context = new EngineContext(catalog);
        this.parser = new SqlParser(context);
        this.executor = new Executor(context);
    }

    public SqlResult execute(String sql) {
        ParsedSql parsedSql = parser.parse(sql);
        if (parsedSql.paramCount() > 0) {
            throw new SqlParseException("即时 execute 不支持 ?,请用prepare()");
        }
        if (parsedSql.statement() instanceof TransactionStatement statement) {
            executeTransaction(statement);
            return SqlResult.of(ExecuteResult.SUCCESS());
        }
        return executor.execute(parsedSql.statement());
    }

    private void executeTransaction(TransactionStatement statement) {
        switch (statement) {
            case BeginStatement _ -> beginTransaction();
            case CommitStatement _ -> commit();
            case RollbackStatement _ -> rollback();
            case SavepointStatement s -> savepoint(s.name());
            case RollbackToSavepointStatement s -> rollbackToSavepoint(s.name());
            case ReleaseSavepointStatement s -> releaseSavepoint(s.name());
        }
    }

    public SqlPreparedStatement prepare(String sql) {
        ParsedSql parsed = parser.parse(sql);
        Statement stmt = parsed.statement();
        if (stmt instanceof DefineStatement) {
            throw new SqlParseException("DDL 不支持预处理");
        }
        return new SqlPreparedStatement(stmt, parsed.paramCount(), executor);
    }

    public void beginTransaction() {
        transactionManager.begin(context);
    }

    public void commit() {
        transactionManager.commit(context);
    }

    public void rollback() {
        transactionManager.rollback(context);
    }

    public boolean inTransaction() {
        return transactionManager.isActive(context);
    }

    public void savepoint(String name) {
        transactionManager.savepoint(context, name);
    }

    public void rollbackToSavepoint(String name) {
        transactionManager.rollbackToSavepoint(context, name);
    }

    public void releaseSavepoint(String name) {
        transactionManager.releaseSavepoint(context, name);
    }
}
