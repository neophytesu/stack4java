package mysql.core.transaction.undo;

import mysql.core.EngineContext;

public record SavepointMark(String name) implements UndoEntry {
    @Override
    public void apply(EngineContext context) {
        throw new UnsupportedOperationException("savepoint 不是可执行的 Undo");
    }
}
