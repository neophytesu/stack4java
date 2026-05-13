package mysql.storage;

import lombok.Data;

import java.util.List;

@Data
public class Table {
    private String tableName;
    private List<Column> columns;
    private List<Row> rows;
}
