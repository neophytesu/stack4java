package jdbc;

public interface Connection extends AutoCloseable {
    PreparedStatement prepareStatement(String sql);

    void close();
}
