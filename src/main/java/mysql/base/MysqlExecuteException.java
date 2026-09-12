package mysql.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MysqlExecuteException extends RuntimeException {
    private Long code;
    private String description;

    public static MysqlExecuteException COLUMN_NOT_FIND(String columnName) {
        return new MysqlExecuteException(101L, "没找到列名为" + columnName + "的列");
    }

    public static MysqlExecuteException COLUMN_NOT_EXITED(String columnName) {
        return new MysqlExecuteException(102L, "列" + columnName + "不存在");
    }

    public static MysqlExecuteException COLUMN_TYPE_NOT_MATCHED(String columnName, Object value) {
        return new MysqlExecuteException(103L, "列" + columnName + "的类型和插入值" + value + "不符");
    }

    public static MysqlExecuteException COLUMN_INDEX_OVER() {
        return new MysqlExecuteException(104L, "列索引越界");
    }

    public static MysqlExecuteException NULL_NOT_SUPPORTED_COMPARE() {
        return new MysqlExecuteException(105L, "该比较不支持NULL");
    }

    public static MysqlExecuteException NULL_NOT_SUPPORTED_COMPUTE() {
        return new MysqlExecuteException(106L, "该计算不支持NULL");
    }

    public static MysqlExecuteException WRONG_TYPE_TO_COMPUTE() {
        return new MysqlExecuteException(107L, "错误的类型用于算数运算");
    }

    public static MysqlExecuteException NEED_TO_PREPARE() {
        return new MysqlExecuteException(108L, "未进行预处理");
    }
}
