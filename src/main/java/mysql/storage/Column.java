package mysql.storage;

import lombok.Data;

@Data
public class Column {
    private String columnName;
    private ColumnType columnType;
}
