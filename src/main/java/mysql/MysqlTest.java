package mysql;

import mysql.ast.statement.*;
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
        executor.executeUpdate(new CreateSchemaStatement("mydb"));
        Table table = new Table();
        table.setTableName("user");
        table.setPrimaryIdx(0);
        table.setColumns(List.of(new Column("id", ColumnType.INTEGER), new Column("name", ColumnType.VARCHAR), new Column("age", ColumnType.INTEGER)));
        table.setRows(new ArrayList<>());
        executor.executeUpdate(new CreateTableStatement("mydb", table));
        executor.executeUpdate(new InsertStatement("mydb", "user", List.of(1, "Alice", 20)));
        System.out.println(executor.executeQuery(new SelectWhereStatement("mydb", "user", "name", "Alice")));
        System.out.println(executor.executeQuery(new SelectAllStatement("mydb", "user")));
        executor.executeUpdate(new UpdateByPrimaryKeyStatement("mydb", "user", 1, "age", 21));
        System.out.println(executor.executeQuery(new SelectColumnsStatement("mydb", "user", List.of("name", "age"))));
        executor.executeUpdate(new DeleteByPrimaryKeyStatement("mydb", "user", 1));
        executor.executeUpdate(new DeleteAllStatement("mydb", "user"));
    }
}
