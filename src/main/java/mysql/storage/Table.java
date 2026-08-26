package mysql.storage;

import lombok.Data;

import java.util.List;

@Data
public class Table {
    Integer primaryIdx;
    long nextAutoIncrement = 1;
    private String tableName;
    private List<Column> columns;
    private List<Row> rows;
}
