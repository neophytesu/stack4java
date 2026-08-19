package mysql.ast.statement;

import java.util.List;

public record InsertStatement(String schemaName, String tableName, List<Object> value) implements Statement {
}
