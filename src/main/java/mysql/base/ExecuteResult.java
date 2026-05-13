package mysql.base;

public record ExecuteResult(Long code, String description) {
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
}
