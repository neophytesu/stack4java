package jdbc;

public interface Connection extends AutoCloseable {
    PreparedStatement prepareStatement(String sql);

    void setAutoCommit(boolean autoCommit);

    boolean getAutoCommit();

    void commit();

    void rollback();

    void close();
}
