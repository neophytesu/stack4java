package mysql;

import mysql.config.SqlEngineBootstrap;
import mysql.core.SqlEngine;
import mysql.core.SqlPreparedStatement;
import mysql.core.SqlResult;

public class MysqlTest {

    static void main() {
        SqlEngine sqlEngine = SqlEngineBootstrap.createAndInit();
        String tableName = "user";
        String sql = """
                INSERT INTO user VALUES (1, 'Alice', 20),(2, 'Bob', 30),(3, 'Dup', 25);
                INSERT INTO user VALUES (NULL, 'Carol', 22);
                SELECT * FROM user WHERE (age > 18 AND id = 1) AND name IS NOT NULL;
                SELECT * FROM user WHERE age > 20 AND name = 'Bob';
                UPDATE user SET age = 21 WHERE name = 'Alice';
                DELETE FROM user WHERE age >= 25;
                SELECT id, name FROM user;
                ALTER TABLE user ADD COLUMN sex VARCHAR;
                INSERT INTO user VALUES (7, 'M', 18, '男');
                ALTER TABLE user DROP COLUMN sex;
                DELETE FROM user""";
        executeSqlList(sqlEngine, sql, tableName);
        SqlPreparedStatement ps = sqlEngine.prepare("INSERT INTO user VALUES (? ,? ,?)");
        ps.setInt(1, 1);
        ps.setString(2, "Alice");
        ps.setInt(3, 20);
        ps.execute();
        ps = sqlEngine.prepare("SELECT * FROM user ORDER BY age DESC LIMIT ?");
        ps.setInt(1, 1);
        SqlResult result = ps.execute();
        System.out.println(result.toString());
        sql = "SELECT * FROM user";
        sqlEngine.beginTransaction();
        sqlEngine.execute("INSERT INTO user VALUES (NULL, 'Carol', 22);");
        sqlEngine.rollback();
        executeSql(sqlEngine, sql, tableName);
        sqlEngine.beginTransaction();
        sqlEngine.execute("INSERT INTO user VALUES (NULL, 'Dup', 25);");
        sqlEngine.commit();
        executeSqlList(sqlEngine, sql, tableName);
    }

    private static void executeSqlList(SqlEngine sqlEngine, String s, String tableName) {
        String[] sqlList = s.split(";");
        for (String sql : sqlList) {
            executeSql(sqlEngine, sql, tableName);
        }
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
