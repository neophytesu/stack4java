package jdbc.support;

import jdbc.Connection;
import jdbc.DataSource;
import mysql.storage.Catalog;
import spring.ioc.bean.lifecycle.destroy.PreDestroy;

public class PooledDataSource implements DataSource {
    ConnectionPool connectionPool;

    public PooledDataSource(Catalog catalog, int maxSize, String defaultSchemaName) {
        connectionPool = new ConnectionPool(catalog, maxSize, defaultSchemaName);
    }

    @Override
    public Connection getConnection() {
        return connectionPool.borrow();
    }

    @PreDestroy
    public void close() {
        connectionPool.close();
    }
}
