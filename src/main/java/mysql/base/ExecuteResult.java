package mysql.base;

import java.util.List;

public record ExecuteResult(Long code, String description) {

    public static ExecuteResult convertException(MysqlExecuteException e) {
        return new ExecuteResult(e.getCode(), e.getDescription());
    }

    public boolean isSuccess() {
        List<Long> successCodes = List.of(1L, 10L, 11L, 12L);
        return successCodes.contains(code);
    }

    public boolean isFailure() {
        return !isSuccess();
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

    public static ExecuteResult Column_NOT_EXIST(String columnName) {
        return new ExecuteResult(7L, "列" + columnName + "不存在");
    }

    public static ExecuteResult COLUMN_COUNT_MISMATCH() {
        return new ExecuteResult(8L, "插入行的列数与表的列数不相等");
    }

    public static ExecuteResult COLUMN_TYPE_MISMATCH(String columnName) {
        return new ExecuteResult(9L, "插入值类型与列" + columnName + "不符");
    }

    public static ExecuteResult UPDATE_SUCCESS(Integer num) {
        return new ExecuteResult(10L, "更新成功，更新" + num + "行");
    }

    public static ExecuteResult DELETE_SUCCESS(Integer num) {
        return new ExecuteResult(11L, "删除成功，删除" + num + "行");
    }

    public static ExecuteResult INSERT_SUCCESS(Integer num) {
        return new ExecuteResult(12L, "插入成功，插入" + num + "行");
    }

    public static ExecuteResult Catalog_NOT_EXIST() {
        return new ExecuteResult(13L, "目录不存在");
    }

    public static ExecuteResult Schema_NOT_USED() {
        return new ExecuteResult(14L, "未选择模式");
    }

    public static ExecuteResult PRIMARY_KEY_IS_NULL() {
        return new ExecuteResult(15L, "主键为NULL");
    }

    public static ExecuteResult PRIMARY_KEY_REPEATED() {
        return new ExecuteResult(16L, "主键重复");
    }
}
