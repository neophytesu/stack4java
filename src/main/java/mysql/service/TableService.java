package mysql.service;

import lombok.Data;
import mysql.base.ExecuteResult;
import mysql.storage.Column;
import mysql.storage.Table;

import java.util.List;

@Data
public class TableService {

    private Table table;

    public ExecuteResult useTable(Table table) {
        this.table = table;
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult addColumn(Column column) {
        List<Column> columns = table.getColumns();
        String columnName = column.getColumnName();
        if (columns.stream().anyMatch(c -> c.getColumnName().equals(columnName))) {
            return ExecuteResult.Column_EXIST();
        }
        columns.add(column);
        return ExecuteResult.SUCCESS();
    }

    public ExecuteResult dropColumn(Column column) {
        List<Column> columns = table.getColumns();
        String columnName = column.getColumnName();
        if (columns.stream().noneMatch(c -> c.getColumnName().equals(columnName))) {
            return ExecuteResult.Column_NOT_EXIST();
        }
        columns.removeIf(c -> c.getColumnName().equals(columnName));
        return ExecuteResult.SUCCESS();
    }
}
