package jdbc.support;

import jdbc.Connection;
import jdbc.JdbcException;

public class ConnectionHolder {
    private static final ThreadLocal<Connection> HOLDER = new ThreadLocal<>();

    public static void bind(Connection connection) {
        if (HOLDER.get() != null) {
            throw new JdbcException("当前线程已绑定连接");
        }
        HOLDER.set(connection);
    }

    public static Connection get() {
        return HOLDER.get();
    }

    public static boolean hasConnection() {
        return HOLDER.get() != null;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
