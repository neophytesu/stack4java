package mysql.core.transaction.undo;

import mysql.core.EngineContext;
import mysql.core.transaction.Transaction;
import mysql.core.transaction.TransactionException;
import mysql.core.transaction.TransactionManager;

public class UndoTransactionManager implements TransactionManager {
    private long nextId = 1;

    @Override
    public void begin(EngineContext context) {
        if (isActive(context)) {
            throw new TransactionException("事务已开启");
        }
        context.setActiveTransaction(new Transaction(nextId++, new UndoLog()));
    }

    @Override
    public void commit(EngineContext context) {
        ensureActive(context).undoLog().clear();
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
        transaction.undoLog().rollbackAll(context);
        context.setActiveTransaction(null);
    }

    @Override
    public boolean isActive(EngineContext context) {
        return context.getActiveTransaction() != null;
    }
}
