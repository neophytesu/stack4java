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
        return new MysqlExecuteException(105L, "null 不支持该比较");
    }
}
