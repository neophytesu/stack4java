package jdbc.support;

import jdbc.JdbcException;
import jdbc.PreparedStatement;
import jdbc.ResultSet;
import mysql.core.SqlEngine;
import mysql.core.SqlPreparedStatement;
import mysql.core.SqlResult;
import mysql.storage.Column;
import mysql.storage.Table;

import java.util.List;

public class SqlEnginePreparedStatement implements PreparedStatement {
    private final SqlPreparedStatement delegate;
    private final SqlEngine sqlEngine;
    private boolean closed;
    private final SqlEngineConnection connection;

    public SqlEnginePreparedStatement(SqlPreparedStatement delegate, SqlEngine sqlEngine, SqlEngineConnection connection) {
        this.delegate = delegate;
        this.sqlEngine = sqlEngine;
        this.connection = connection;
    }

    @Override
    public void setObject(int index, Object value) {
        delegate.setObject(index, value);
    }

    @Override
    public void setInt(int index, int value) {
        delegate.setInt(index, value);
    }

    @Override
    public void setString(int index, String value) {
        delegate.setString(index, value);
    }

    @Override
    public void setNull(int index) {
        delegate.setNull(index);
    }

    @Override
    public ResultSet executeQuery() {
        checkOpen();
        connection.ensureTransactionStarted();
        SqlResult result = delegate.execute();
        if (!result.isSuccess()) {
            throw new JdbcException(result.executeResult().description());
        }
        if (!result.isQuery()) {
            throw new JdbcException("不是查询语句");
        }
        return new SqlEngineResultSet(currentColumnNames(), result.rows());
    }

    private List<String> currentColumnNames() {
        Table table = sqlEngine.getContext().getCurrentTable();
        if (table == null) {
            throw new JdbcException("无法获取列信息");
        }
        return table.getColumns().stream().map(Column::getColumnName).toList();
    }

    private void checkOpen() {
        if (closed) {
            throw new JdbcException("PreparedStatement 已关闭");
        }
    }

    @Override
    public int executeUpdate() {
        checkOpen();
        connection.ensureTransactionStarted();
        SqlResult result = delegate.execute();
        if (!result.isSuccess()) {
            throw new JdbcException(result.executeResult().description());
        }
        if (result.isQuery()) {
            throw new JdbcException("不是更新语句");
        }
        return result.executeResult().affectedRows();
    }

    @Override
    public void close() {
        closed = true;
    }
}
