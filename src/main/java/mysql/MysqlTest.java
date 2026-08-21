package mysql;

import mysql.ast.parser.SqlParser;
import mysql.ast.statement.*;
import mysql.core.EngineContext;
import mysql.core.Executor;
import mysql.storage.*;

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
        SqlParser parser = new SqlParser(context);
        executor.executeDefine(parser.parseDdl("CREATE SCHEMA mydb"));
        executor.executeDefine(parser.parseDdl("USE mydb"));
        Table table = new Table();
        table.setTableName("user");
        table.setPrimaryIdx(0);
        table.setColumns(List.of(new Column("id", ColumnType.INTEGER), new Column("name", ColumnType.VARCHAR), new Column("age", ColumnType.INTEGER)));
        table.setRows(new ArrayList<>());
        executor.executeDefine(new CreateTableStatement("mydb", table));
        executor.executeUpdate(parser.parseDml("INSERT INTO user VALUES (1, 'Alice', 20)"));
        List<Row> rows = executor.executeQuery(
                parser.parseDql("SELECT * FROM user WHERE name = 'Alice'")
        );
        for (Row row : rows) {
            System.out.println(row.toString());
        }
    }
}
