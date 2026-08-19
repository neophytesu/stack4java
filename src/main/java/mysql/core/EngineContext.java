package mysql.core;

import lombok.Data;
import mysql.base.ExecuteResult;
import mysql.service.CatalogService;
import mysql.service.SchemaService;
import mysql.service.TableService;
import mysql.storage.Catalog;
import mysql.storage.Schema;
import mysql.storage.Table;

@Data
public class EngineContext {
    private Catalog catalog;

    public EngineContext(Catalog catalog) {
        this.catalog = catalog;
        catalogService = new CatalogService();
        schemaService = new SchemaService();
        tableService = new TableService();
        catalogService.useCatalog(catalog);
    }

    private CatalogService catalogService;
    private SchemaService schemaService;
    private TableService tableService;

    private Schema currentSchema;
    private Table currentTable;

    public ExecuteResult useSchema(String schemaName) {
        if (catalog == null) {
            return ExecuteResult.Catalog_NOT_EXIST();
        }
        Schema schema = catalog.getSchemas().get(schemaName);
        if (schema == null) {
            return ExecuteResult.Schema_NOT_EXIST();
        }
        currentSchema = schema;
        currentTable = null;
        tableService.useTable(null);
        schemaService.useSchema(schema);
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult useTable(String tableName) {
        if (currentSchema == null) {
            return ExecuteResult.Schema_NOT_EXIST();
        }
        Table table = currentSchema.getTables().get(tableName);
        if (table == null) {
            return ExecuteResult.TABLE_NOT_EXIST();
        }
        currentTable = table;
        tableService.useTable(table);
        return ExecuteResult.SUCCESS();
    }
}
