package mysql.core;

import lombok.Getter;
import mysql.ast.parser.SqlParser;
import mysql.storage.Catalog;

public class SqlEngine {
    @Getter
    private final EngineContext context;
    private final SqlParser parser;
    private final Executor executor;

    public SqlEngine(Catalog catalog) {
        this.context = new EngineContext(catalog);
        this.parser = new SqlParser(context);
        this.executor = new Executor(context);
    }

    public SqlResult execute(String sql) {
        return executor.execute(parser.parse(sql));
    }
}
