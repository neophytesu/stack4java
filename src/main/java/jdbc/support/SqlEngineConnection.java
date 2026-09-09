package jdbc.support;

import jdbc.Connection;
import jdbc.JdbcException;
import jdbc.PreparedStatement;
import mysql.core.SqlEngine;
import mysql.core.SqlPreparedStatement;

public class SqlEngineConnection implements Connection {
    private final SqlEngine sqlEngine;
    private boolean closed;

    public SqlEngineConnection(SqlEngine sqlEngine) {
        this.sqlEngine = sqlEngine;
    }

    @Override
    public PreparedStatement prepareStatement(String sql) {
        checkOpen();
        SqlPreparedStatement delegate = sqlEngine.prepare(sql);
        return new SqlEnginePreparedStatement(delegate, sqlEngine);
    }

    private void checkOpen() {
        if (closed) {
            throw new JdbcException("Connection 已关闭");
        }
    }

    @Override
    public void close() {
        closed = true;
    }
}
