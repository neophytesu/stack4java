package mysql.core.transaction;

import mysql.core.EngineContext;
import mysql.storage.Catalog;

public class SnapshotTransactionManager implements TransactionManager {
    private long nextId = 1;

    @Override
    public void begin(EngineContext context) {
        if (isActive(context)) {
            throw new TransactionException("事务已开启");
        }
        Catalog snapshot = CatalogSnapshot.copy(context.getCatalog());
        context.setActiveTransaction(new Transaction(nextId++, snapshot));
    }

    @Override
    public void commit(EngineContext context) {
        ensureActive(context);
        context.setActiveTransaction(null);
    }

    private Transaction ensureActive(EngineContext context) {
        Transaction transaction = context.getActiveTransaction();
        if (transaction == null) {
            throw new TransactionException("无活跃事务");
        }
        return transaction;
    }

    @Override
    public void rollback(EngineContext context) {
        Transaction transaction = ensureActive(context);
        context.setCatalog(CatalogSnapshot.copy(transaction.rollbackSnapshot()));
        context.rebindAfterStorageChange();
        context.setActiveTransaction(null);
    }

    @Override
    public boolean isActive(EngineContext context) {
        return context.getActiveTransaction() != null;
    }
}
