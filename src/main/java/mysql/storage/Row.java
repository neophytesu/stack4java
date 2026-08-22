package mysql.storage;

import lombok.Data;

import java.util.Arrays;

@Data
public class Row {
    Object[] values;

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
