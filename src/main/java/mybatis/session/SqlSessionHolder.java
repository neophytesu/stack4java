package mybatis.session;

public class SqlSessionHolder {
    private static final ThreadLocal<SqlSession> HOLDER = new ThreadLocal<>();

    public static void bind(SqlSession session) {
        if (HOLDER.get() != null) {
            throw new IllegalStateException("当前线程已绑定 SqlSession");
        }
        HOLDER.set(session);
    }

    public static SqlSession get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
