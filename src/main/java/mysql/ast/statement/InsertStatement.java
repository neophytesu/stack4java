package mysql.ast.statement;

import java.util.List;

public record InsertStatement(String schemaName, String tableName, List<String> columnNames,
                              List<Object> values) implements ManipulateStatement {
}
