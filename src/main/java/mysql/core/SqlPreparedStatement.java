package mysql.core;

import mysql.ast.parser.SqlParseException;
import mysql.ast.statement.Statement;

import java.util.Arrays;

public class SqlPreparedStatement {
    private final Statement template;
    private final int paramCount;
    private final Executor executor;
    private final Object[] params;
    private final boolean[] bound;

    public SqlPreparedStatement(Statement template, int paramCount, Executor executor) {
        this.template = template;
        this.paramCount = paramCount;
        this.executor = executor;
        this.params = new Object[paramCount];
        this.bound = new boolean[paramCount];
    }

    public void setObject(int index, Object value) {
        checkIndex(index);
        int i = index - 1;
        params[i] = value;
        bound[i] = true;
    }

    public void setNull(int index) {
        setObject(index, null);
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
        Arrays.fill(bound, false);
    }

    private void checkAllBound() {
        for (int i = 0; i < paramCount; i++) {
            if (!bound[i]) {
                throw new SqlParseException("参数 " + (i + 1) + " 未绑定");
            }
        }
    }
}
