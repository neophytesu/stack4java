package jdbc.support;

import jdbc.Connection;
import jdbc.DataSource;
import mysql.core.SqlEngine;

public class SqlEngineDataSource implements DataSource {
    private final SqlEngine sqlEngine;

    public SqlEngineDataSource(SqlEngine sqlEngine) {
        this.sqlEngine = sqlEngine;
    }

    @Override
    public Connection getConnection() {
        return new SqlEngineConnection(sqlEngine);
    }
}
