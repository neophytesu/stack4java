package jdbc.support;

import jdbc.JdbcException;
import jdbc.ResultSet;
import mysql.storage.Row;

import java.util.ArrayList;
import java.util.List;

public class SqlEngineResultSet implements ResultSet {
    private final List<String> columnNames;
    private final List<Row> rows;
    private int cursor = -1;
    private boolean closed;

    public SqlEngineResultSet(List<String> columnNames, List<Row> rows) {
        this.columnNames = columnNames;
        this.rows = rows == null ? new ArrayList<>() : rows;
    }

    @Override
    public boolean next() {
        cursor++;
        return cursor < rows.size();
    }

    @Override
    public int getInt(String column) {
        return (Integer) getObject(column);
    }

    @Override
    public String getString(String column) {
        return (String) getObject(column);
    }

    @Override
    public Object getObject(String column) {
        int idx = columnNames.indexOf(column);
        if (idx < 0) {
            throw new JdbcException("列不存在：" + column);
        }
        return rows.get(cursor).getValues()[idx];
    }

    @Override
    public void close() {
        closed = true;
    }
}
