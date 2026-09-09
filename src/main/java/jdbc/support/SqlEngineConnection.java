package jdbc.support;

import jdbc.Connection;
import jdbc.JdbcException;
import jdbc.PreparedStatement;
import mysql.core.SqlEngine;
import mysql.core.SqlPreparedStatement;

public class SqlEngineConnection implements Connection {
    private final SqlEngine sqlEngine;
    private boolean closed = false;
    private boolean autoCommit = true;

    public SqlEngineConnection(SqlEngine sqlEngine) {
        this.sqlEngine = sqlEngine;
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
        if (!autoCommit && sqlEngine.inTransaction()) {
            sqlEngine.rollback();
        }
        closed = true;
    }

    public void ensureTransactionStarted() {
        if (!autoCommit && !sqlEngine.inTransaction()) {
            sqlEngine.beginTransaction();
        }
    }
}
