package mysql.base;

public record ExecuteResult(Long code, String description) {

    public static ExecuteResult convertException(MysqlExecuteException e) {
        return new ExecuteResult(e.getCode(), e.getDescription());
    }

    public static ExecuteResult SUCCESS() {
        return new ExecuteResult(1L, "执行成功");
    }

    public static ExecuteResult TABLE_EXIST() {
        return new ExecuteResult(2L, "表已存在");
    }

    public static ExecuteResult TABLE_NOT_EXIST() {
        return new ExecuteResult(3L, "表不存在");
    }

    public static ExecuteResult Schema_EXIST() {
        return new ExecuteResult(4L, "模式已存在");
    }

    public static ExecuteResult Schema_NOT_EXIST() {
        return new ExecuteResult(5L, "模式不存在");
    }

    public static ExecuteResult Column_EXIST() {
        return new ExecuteResult(6L, "列已存在");
    }

    public static ExecuteResult Column_NOT_EXIST() {
        return new ExecuteResult(7L, "列不存在");
    }

    public static ExecuteResult COLUMN_COUNT_MISMATCH() {
        return new ExecuteResult(8L, "插入行的列数与表的列数不相等");
    }

    public static ExecuteResult COLUMN_TYPE_MISMATCH(String columnName) {
        return new ExecuteResult(9L, "插入值类型与列" + columnName + "不符");
    }

}
