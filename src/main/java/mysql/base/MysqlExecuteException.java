package mysql.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MysqlExecuteException extends RuntimeException{
    private Long code;
    private String description;
}
