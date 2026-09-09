package mysql.core.transaction;

import mysql.storage.*;
import mysql.utils.MysqlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogSnapshot {
    private CatalogSnapshot() {

    }

    public static Catalog copy(Catalog source) {
        if (source == null) {
            return null;
        }
        Catalog copy = new Catalog();
        copy.setName(source.getName());
        HashMap<String, Schema> schemaMap = new HashMap<>();
        if (source.getSchemas() != null) {
            for (Map.Entry<String, Schema> e : source.getSchemas().entrySet()) {
                schemaMap.put(e.getKey(), copySchema(e.getValue()));
            }
        }
        copy.setSchemas(schemaMap);
        return copy;
    }

    private static Schema copySchema(Schema source) {
        Schema copy = new Schema();
        copy.setSchemaName(source.getSchemaName());
        Map<String, Table> tableMap = new HashMap<>();
        if (source.getTables() != null) {
            for (Map.Entry<String, Table> e : source.getTables().entrySet()) {
                tableMap.put(e.getKey(), copyTable(e.getValue()));
            }
        }
        copy.setTables(tableMap);
        return copy;
    }

    private static Table copyTable(Table source) {
        Table copy = new Table();
        copy.setTableName(source.getTableName());
        copy.setPrimaryIdx(source.getPrimaryIdx());
        copy.setNextAutoIncrement(source.getNextAutoIncrement());
        List<Column> columnsCopy = new ArrayList<>();
        if (source.getColumns() != null) {
            for (Column c : source.getColumns()) {
                columnsCopy.add(new Column(
                        c.getColumnName(),
                        c.getColumnType(),
                        c.isAutoIncrement()
                ));
            }
        }
        copy.setColumns(columnsCopy);
        List<Row> rowsCopy = new ArrayList<>();
        if (source.getRows() != null) {
            for (Row row : source.getRows()) {
                rowsCopy.add(copyRow(row, columnsCopy));
            }
        }
        copy.setRows(rowsCopy);
        return copy;
    }

    private static Row copyRow(Row source, List<Column> columns) {
        Row copy = new Row();
        if (source.getValues() == null) {
            copy.setValues(null);
            return copy;
        }
        Object[] valuesCopy = new Object[source.getValues().length];
        for (int i = 0; i < source.getValues().length; i++) {
            ColumnType type = columns.get(i).getColumnType();
            valuesCopy[i] = MysqlUtil.deepCopyValue(source.getValues()[i], type);
        }
        copy.setValues(valuesCopy);
        return copy;
    }
}
