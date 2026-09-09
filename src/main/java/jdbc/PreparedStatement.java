package jdbc;

public interface PreparedStatement extends AutoCloseable {
    void close();

    void setObject(int index, Object value);

    void setInt(int index, int value);

    void setString(int index, String value);

    void setNull(int index);

    ResultSet executeQuery();

    int executeUpdate();
}
