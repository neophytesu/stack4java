package mysql.storage;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Schema {
    private String schemaName;
    private Map<String, Table> tables = new HashMap<>();
}
