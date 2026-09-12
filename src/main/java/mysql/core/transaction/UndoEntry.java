package mysql.core.transaction;

import mysql.core.EngineContext;

public interface UndoEntry {
    void apply(EngineContext context);
}
