package mysql.core.transaction;

import mysql.core.EngineContext;

public interface TransactionManager {
    void begin(EngineContext context);

    void commit(EngineContext context);

    void rollback(EngineContext context);

    boolean isActive(EngineContext context);
}
