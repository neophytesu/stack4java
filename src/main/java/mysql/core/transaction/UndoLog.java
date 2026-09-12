package mysql.core.transaction;

import mysql.core.EngineContext;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoLog {
    Deque<UndoEntry> entries = new ArrayDeque<>();

    public void add(UndoEntry entry) {
        entries.addLast(entry);
    }

    public void rollbackAll(EngineContext ctx) {
        while (!entries.isEmpty()) {
            entries.removeLast().apply(ctx);
        }
    }

    public void clear() {
        entries.clear();
    }
}
