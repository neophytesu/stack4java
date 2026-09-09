package jdbc;

public interface ResultSet extends AutoCloseable {
    boolean next();

    int getInt(String column);

    String getString(String column);

    Object getObject(String column);

    void close();
}
