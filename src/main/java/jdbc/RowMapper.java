package jdbc;

@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet rs, int rowNum);
}
