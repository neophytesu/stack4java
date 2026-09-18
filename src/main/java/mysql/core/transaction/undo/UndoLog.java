package mysql.core.transaction.undo;

import mysql.core.EngineContext;
import mysql.core.transaction.TransactionException;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoLog {
    Deque<UndoEntry> entries = new ArrayDeque<>();

    public void add(UndoEntry entry) {
        entries.addLast(entry);
    }

    public void rollbackAll(EngineContext ctx) {
        while (!entries.isEmpty()) {
            UndoEntry undoEntry = entries.removeLast();
            if (undoEntry instanceof SavepointMark) {
                continue;
            }
            undoEntry.apply(ctx);
        }
    }

    public void savepoint(String name) {
        entries.removeIf(e -> e instanceof SavepointMark(String s) && s.equals(name));
        entries.addLast(new SavepointMark(name));
    }

    public void rollbackTo(EngineContext context, String name) {
        while (!entries.isEmpty()) {
            UndoEntry undoEntry = entries.peekLast();
            if (undoEntry instanceof SavepointMark(String s) && s.equals(name)) {
                return;
            }
            entries.removeLast();
            if (undoEntry instanceof SavepointMark) {
                continue;
            }
            undoEntry.apply(context);
        }
        throw new TransactionException("savepoint 不存在: " + name);
    }

    public void release(String name) {
        boolean removed = entries.removeIf(e -> e instanceof SavepointMark(String s) && s.equals(name));
        if (!removed) {
            throw new TransactionException("savepoint 不存在: " + name);
        }
    }

    public void clear() {
        entries.clear();
    }
}
