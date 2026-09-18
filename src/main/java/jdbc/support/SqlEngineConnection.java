package jdbc.support;

import jdbc.Connection;
import jdbc.JdbcException;
import jdbc.PreparedStatement;
import jdbc.Savepoint;
import lombok.Getter;
import mysql.core.SqlEngine;
import mysql.core.SqlPreparedStatement;

public class SqlEngineConnection implements Connection {
    @Getter
    private final SqlEngine sqlEngine;
    private boolean closed = false;
    private boolean autoCommit = true;
    private ConnectionPool connectionPool = null;

    public SqlEngineConnection(SqlEngine sqlEngine) {
        this.sqlEngine = sqlEngine;
    }

    public SqlEngineConnection(SqlEngine engine, ConnectionPool connectionPool) {
        this.sqlEngine = engine;
        this.connectionPool = connectionPool;
    }

    @Override
    public PreparedStatement prepareStatement(String sql) {
        checkOpen();
        SqlPreparedStatement delegate = sqlEngine.prepare(sql);
        return new SqlEnginePreparedStatement(delegate, sqlEngine, this);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        checkOpen();
        if (autoCommit) {
            if (this.autoCommit) {
                return;
            }
            if (sqlEngine.inTransaction()) {
                sqlEngine.commit();
            }
            this.autoCommit = true;
        } else {
            this.autoCommit = false;
        }
    }

    @Override
    public boolean getAutoCommit() {
        return autoCommit;
    }

    @Override
    public void commit() {
        checkOpen();
        if (!sqlEngine.inTransaction()) {
            throw new JdbcException("无活跃事务");
        }
        sqlEngine.commit();
    }

    @Override
    public void rollback() {
        checkOpen();
        if (!sqlEngine.inTransaction()) {
            throw new JdbcException("无活跃事务");
        }
        sqlEngine.rollback();
    }

    private void checkOpen() {
        if (closed) {
            throw new JdbcException("Connection 已关闭");
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        if (connectionPool != null) {
            connectionPool.recycle(this);
        } else {
            if (!autoCommit && sqlEngine.inTransaction()) {
                sqlEngine.rollback();
            }
        }
        closed = true;
    }

    public void ensureTransactionStarted() {
        if (!autoCommit && !sqlEngine.inTransaction()) {
            sqlEngine.beginTransaction();
        }
    }

    public boolean isInTransaction() {
        return sqlEngine.inTransaction();
    }

    @Override
    public Savepoint setSavepoint(String name) {
        checkOpen();
        ensureSavepointAllowed();
        ensureTransactionStarted();
        try {
            sqlEngine.savepoint(name);
        } catch (RuntimeException e) {
            throw new JdbcException(e.getMessage());
        }
        return new Savepoint(name);
    }

    private void ensureSavepointAllowed() {
        if (autoCommit) {
            throw new JdbcException("autoCommit 下不能使用 savepoint");
        }
    }

    @Override
    public void rollback(Savepoint savepoint) {
        checkOpen();
        ensureSavepointAllowed();
        if (savepoint == null || savepoint.name() == null) {
            throw new JdbcException("savepoint 不能为空");
        }
        try {
            sqlEngine.rollbackToSavepoint(savepoint.name());
        } catch (Exception e) {
            throw new JdbcException(e.getMessage());
        }
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) {
        checkOpen();
        ensureSavepointAllowed();
        if (savepoint == null || savepoint.name() == null) {
            throw new JdbcException("savepoint 不能为空");
        }
        try {
            sqlEngine.releaseSavepoint(savepoint.name());
        } catch (RuntimeException e) {
            throw new JdbcException(e.getMessage());
        }
    }

    public void resetForReuse() {
        autoCommit = true;
        closed = false;
    }

    public void destroyPhysical() {
        if (isInTransaction()) {
            sqlEngine.rollback();
        }
        connectionPool = null;
        closed = true;
    }
}
