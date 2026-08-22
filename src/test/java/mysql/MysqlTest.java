package mysql;

import mysql.ast.parser.SqlParser;
import mysql.ast.statement.CreateTableStatement;
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
        SqlParser parser = new SqlParser(context);
        executor.executeDefine(parser.parseDdl("CREATE SCHEMA myDb"));
        executor.executeDefine(parser.parseDdl("USE myDb"));
        String sql = "CREATE TABLE user (id INT, name VARCHAR, age INT, PRIMARY KEY (id))";
        System.out.println(sql);
        executor.executeDefine(parser.parseDdl(sql));
        sql = "INSERT INTO user VALUES (1, 'Alice', 20)";
        System.out.println(sql);
        executor.executeUpdate(parser.parseDml(sql));
        sql = "INSERT INTO user VALUES (2, 'Bob', 30)";
        System.out.println(sql);
        executor.executeUpdate(parser.parseDml(sql));
        printTable(executor, parser, "user");

        sql = "SELECT * FROM user WHERE name = 'Alice'";
        var rows = executor.executeQuery(
                parser.parseDql(sql));
        System.out.println(sql);
        System.out.println(rows);

        sql = "SELECT * FROM user WHERE age > 20 AND name = 'Bob'";
        rows = executor.executeQuery(
                parser.parseDql(sql));
        System.out.println(sql);
        System.out.println(rows);

        sql = "UPDATE user SET age = 21 WHERE name = 'Alice'";
        executor.executeUpdate(parser.parseDml(sql));
        System.out.println(sql);
        printTable(executor, parser, "user");

        sql = "DELETE FROM user WHERE age >= 25";
        executor.executeUpdate(parser.parseDml(sql));
        System.out.println(sql);
        printTable(executor, parser, "user");

        sql = "SELECT id, name FROM user";
        rows = executor.executeQuery(parser.parseDql(sql));
        System.out.println(sql);
        System.out.println(rows);

        sql = "DELETE FROM user";
        executor.executeUpdate(parser.parseDml(sql));
        System.out.println(sql);
        printTable(executor, parser, "user");
    }

    private static void printTable(Executor executor, SqlParser parser, String tableName) {
        System.out.println(executor.executeQuery(parser.parseDql("SELECT * FROM " + tableName)));
    }
}
