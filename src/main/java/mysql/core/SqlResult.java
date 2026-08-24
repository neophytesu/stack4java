package mysql.core;

import mysql.base.ExecuteResult;
import mysql.storage.Row;

import java.util.List;

public record SqlResult(ExecuteResult executeResult, List<Row> rows) {
    public static SqlResult of(ExecuteResult executeResult) {
        return new SqlResult(executeResult, null);
    }

    public static SqlResult of(List<Row> rows) {
        return new SqlResult(ExecuteResult.SUCCESS(), rows);
    }

    public boolean isSuccess() {
        return executeResult.isSuccess();
    }

    public boolean isQuery() {
        return rows != null;
    }

}
