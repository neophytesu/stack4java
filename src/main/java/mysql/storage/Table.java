package mysql.storage;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Table {
    Integer primaryIdx;
    private String tableName;
    private List<Column> columns;
    private List<Row> rows;
}
