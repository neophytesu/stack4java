package mysql.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class Row {
    Object[] values;

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
