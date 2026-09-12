package jdbc.support;

import jdbc.JdbcException;
import mysql.base.ExecuteResult;
import mysql.core.SqlEngine;
import mysql.storage.Catalog;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionPool {
    Catalog sharedCatalog;
    int maxSize;
    Queue<SqlEngineConnection> idle;
    int activeCount;
    volatile boolean shutdown = false;
    String defaultSchemaName;

    public ConnectionPool(Catalog catalog, int maxSize, String defaultSchemaName) {
        this.sharedCatalog = catalog;
        this.maxSize = maxSize;
        idle = new ConcurrentLinkedQueue<>();
        this.defaultSchemaName = defaultSchemaName;
    }

    SqlEngineConnection borrow() {
        if (shutdown) {
            throw new JdbcException("连接池已关闭");
        }
        SqlEngineConnection conn = idle.poll();
        if (conn == null) {
            if (activeCount >= maxSize) {
                throw new JdbcException("连接池已满");
            }
            SqlEngine engine = new SqlEngine(sharedCatalog);
            conn = new SqlEngineConnection(engine, this);
            activeCount++;
        }
        ExecuteResult r = conn.getSqlEngine().getContext().useSchema(defaultSchemaName);
        if (!r.isSuccess()) {
            throw new JdbcException(r.description());
        }
        conn.resetForReuse();
        return conn;
    }

    public void recycle(SqlEngineConnection conn) {
        if (shutdown) {
            conn.destroyPhysical();
            return;
        }
        if (conn.isInTransaction()) {
            conn.rollback();
        }
        idle.offer(conn);
    }

    public void close() {
        shutdown = true;
        SqlEngineConnection conn;
        while ((conn = idle.poll()) != null) {
            conn.destroyPhysical();
        }
    }
}
