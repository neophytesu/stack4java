package mysql.core;

import mysql.base.ExecuteResult;
import mysql.storage.Row;

import java.util.List;

public record SqlResult(ExecuteResult executeResult, List<Row> rows, Integer affectedRows) {
    public static SqlResult of(ExecuteResult executeResult) {
        return new SqlResult(executeResult, null, executeResult.affectedRows());
    }

    public static SqlResult of(List<Row> rows) {
        return new SqlResult(ExecuteResult.SUCCESS(), rows, 0);
    }

    public boolean isSuccess() {
        return executeResult.isSuccess();
    }

    public boolean isQuery() {
        return rows != null;
    }

}
