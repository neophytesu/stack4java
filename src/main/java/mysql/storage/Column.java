package mysql.storage;

public record Column(String columnName, int index, ColumnType columnType) {
}
