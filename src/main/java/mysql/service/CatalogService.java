package mysql.service;

import lombok.Data;
import mysql.base.ExecuteResult;
import mysql.storage.Catalog;
import mysql.storage.Schema;

import java.util.Map;

@Data
public class CatalogService {
    private Catalog catalog;

    public ExecuteResult useCatalog(Catalog catalog) {
        this.catalog = catalog;
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult createSchema(Schema schema) {
        Map<String, Schema> schemas = catalog.getSchemas();
        String schemaName = schema.getSchemaName();
        if (schemas.containsKey(schemaName)) {
            return ExecuteResult.Schema_EXIST();
        }
        schemas.put(schemaName, schema);
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult dropSchema(Schema schema) {
        Map<String, Schema> schemas = catalog.getSchemas();
        String schemaName = schema.getSchemaName();
        if (!schemas.containsKey(schemaName)) {
            return ExecuteResult.Schema_NOT_EXIST();
        }
        schemas.remove(schemaName);
        return ExecuteResult.SUCCESS();
    }
}
