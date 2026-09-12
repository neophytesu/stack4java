package mysql.core.transaction;

public record Transaction(long id, UndoLog undoLog) {
}
