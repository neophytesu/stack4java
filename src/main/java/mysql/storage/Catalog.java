package mysql.storage;

import lombok.Data;

import java.util.Map;

@Data
public class Catalog {
    private String name;
    private Map<String, Schema> schemas;
}
