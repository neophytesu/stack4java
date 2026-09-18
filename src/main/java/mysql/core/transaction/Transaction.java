package mysql.core.transaction;

import mysql.core.transaction.undo.UndoLog;

public record Transaction(long id, UndoLog undoLog) {
}
