package mysql.core.transaction.undo;

import mysql.core.EngineContext;

public interface UndoEntry {
    void apply(EngineContext context);
}
