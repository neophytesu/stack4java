package mysql.storage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Column {
    private String columnName;
    private ColumnType columnType;
}
