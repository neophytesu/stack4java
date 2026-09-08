package mysql.config;

import mysql.core.SqlEngine;
import mysql.storage.Catalog;

import java.util.HashMap;

public class SqlEngineBootstrap {
    public static SqlEngine createAndInit() {
        Catalog catalog = new Catalog();
        catalog.setName("default");
        catalog.setSchemas(new HashMap<>());
        SqlEngine sqlEngine = new SqlEngine(catalog);
        sqlEngine.execute("CREATE SCHEMA app");
        sqlEngine.execute("USE app");
        sqlEngine.execute("""
                CREATE TABLE user (
                    id INT AUTO_INCREMENT,
                    name VARCHAR,
                    age INT,
                    PRIMARY KEY (id)
                )
                """);
        return sqlEngine;
    }
}
