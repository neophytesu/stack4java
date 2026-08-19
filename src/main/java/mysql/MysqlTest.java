package mysql;

import mysql.ast.CreateSchemaStatement;
import mysql.ast.CreateTableStatement;
import mysql.ast.InsertStatement;
import mysql.ast.SelectWhereStatement;
import mysql.core.EngineContext;
import mysql.core.Executor;
import mysql.storage.Catalog;
import mysql.storage.Column;
import mysql.storage.ColumnType;
import mysql.storage.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MysqlTest {
    static void main() {
        Catalog catalog = new Catalog();
        catalog.setName("default");
        catalog.setSchemas(new HashMap<>());
        EngineContext context = new EngineContext(catalog);
        Executor executor = new Executor(context);
        executor.execute(new CreateSchemaStatement("mydb"));
        Table table = new Table();
        table.setTableName("user");
        table.setPrimaryIdx(0);
        table.setColumns(List.of(new Column("id", ColumnType.INTEGER), new Column("name", ColumnType.VARCHAR), new Column("age", ColumnType.INTEGER)));
        table.setRows(new ArrayList<>());
        executor.execute(new CreateTableStatement("mydb", table));
        executor.execute(new InsertStatement("mydb", "user", List.of(1, "Alice", 20)));
        System.out.println(executor.execute(new SelectWhereStatement("mydb", "user", "name", "Alice")));
    }
}
