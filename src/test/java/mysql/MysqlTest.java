package mysql;

import mysql.core.SqlEngine;
import mysql.core.SqlResult;
import mysql.storage.Catalog;

import java.util.HashMap;

public class MysqlTest {

    static void main() {
        Catalog catalog = new Catalog();
        catalog.setName("default");
        catalog.setSchemas(new HashMap<>());
        SqlEngine sqlEngine = new SqlEngine(catalog);
        sqlEngine.execute("CREATE SCHEMA myDb");
        sqlEngine.execute("USE myDb");
        String sql = "CREATE TABLE user (id INT, name VARCHAR, age INT, PRIMARY KEY (id))";
        String tableName = "user";
        System.out.println(sql);
        sqlEngine.execute(sql);
        sql = "INSERT INTO user VALUES (1, 'Alice', 20)";
        executeSql(sqlEngine, sql, tableName);
        sql = "INSERT INTO user VALUES (2, 'Bob', 30)";
        executeSql(sqlEngine, sql, tableName);
        sql = "SELECT * FROM user WHERE (age > 18 AND id = 1) AND name IS NOT NULL";
        executeSql(sqlEngine, sql, tableName);
        sql = "SELECT * FROM user WHERE age > 20 AND name = 'Bob'";
        executeSql(sqlEngine, sql, tableName);
        sql = "UPDATE user SET age = 21 WHERE name = 'Alice'";
        executeSql(sqlEngine, sql, tableName);
        sql = "DELETE FROM user WHERE age >= 25";
        executeSql(sqlEngine, sql, tableName);
        sql = "SELECT id, name FROM user";
        executeSql(sqlEngine, sql, tableName);
        sql = "DELETE FROM user";
        executeSql(sqlEngine, sql, tableName);
    }

    private static void executeSql(SqlEngine sqlEngine, String sql, String tableName) {
        SqlResult sqlResult = sqlEngine.execute(sql);
        if (!sqlResult.isSuccess()) {
            System.err.println(sqlResult.executeResult().description());
            return;
        }
        System.out.println(sql);
        if (sqlResult.isQuery()) {
            System.out.println(sqlResult.rows());
        } else {
            System.out.println(sqlEngine.execute("SELECT * FROM " + tableName).rows());
        }
    }
}
