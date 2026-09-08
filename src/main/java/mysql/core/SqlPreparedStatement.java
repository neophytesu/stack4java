package mysql.core;

import mysql.ast.parser.SqlParseException;
import mysql.ast.statement.Statement;

import java.util.Arrays;

public class SqlPreparedStatement {
    private final Statement template;
    private final int paramCount;
    private final Executor executor;
    private final Object[] params;

    public SqlPreparedStatement(Statement template, int paramCount, Executor executor) {
        this.template = template;
        this.paramCount = paramCount;
        this.executor = executor;
        this.params = new Object[paramCount];
    }

    public void setObject(int index, Object value) {
        checkIndex(index);
        params[index - 1] = value;
    }

    private void checkIndex(int index) {
        if (index < 1 || index > paramCount) {
            throw new SqlParseException("参数索引越界: " + index + ", 共 " + paramCount + " 个");
        }
    }

    public void setInt(int index, int value) {
        setObject(index, value);
    }

    public void setString(int index, String value) {
        setObject(index, value);
    }

    public SqlResult execute() {
        checkAllBound();
        Statement resolved = PlaceholderResolver.resolve(template, params);
        return executor.execute(resolved);
    }

    public void clearParameters() {
        Arrays.fill(params, null);
    }

    private void checkAllBound() {
        for (int i = 0; i < paramCount; i++) {
            if (params[i] == null) {
                throw new SqlParseException("参数 " + (i + 1) + " 未绑定");
            }
        }
    }
}
