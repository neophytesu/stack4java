package mysql.core.transaction;

import mysql.core.EngineContext;

public interface TransactionManager {
    void begin(EngineContext context);

    void commit(EngineContext context);

    void rollback(EngineContext context);

    boolean isActive(EngineContext context);

    void savepoint(EngineContext context, String name);

    void rollbackToSavepoint(EngineContext context, String name);

    void releaseSavepoint(EngineContext context, String name);
}
