package jdbc.support;

import jdbc.Connection;
import jdbc.DataSource;

public class ConnectionUtils {
    public static Connection getConnection(DataSource dataSource) {
        Connection bound = ConnectionHolder.get();
        if (bound != null) {
            return bound;
        }
        return dataSource.getConnection();
    }

    public static void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        if (connection == ConnectionHolder.get()) {
            return;
        }
        connection.close();
    }
}
