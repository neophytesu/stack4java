package spring.aop.interceptor;

import jdbc.Connection;
import jdbc.DataSource;
import jdbc.support.ConnectionHolder;

import java.lang.reflect.InvocationTargetException;

public class TransactionalInterceptor implements MethodInterceptor {
    private final DataSource dataSource;

    public TransactionalInterceptor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(MethodInvocation invocation) {
        Connection conn = dataSource.getConnection();
        ConnectionHolder.bind(conn);
        conn.setAutoCommit(false);
        try {
            Object result = invocation.proceed();
            commitIfActive(conn);
            return result;
        } catch (Exception e) {
            rollbackIfActive(conn);
            if (e instanceof InvocationTargetException t){
                throw new RuntimeException(t.getTargetException());
            }
            throw new RuntimeException(e);
        } finally {
            ConnectionHolder.clear();
            conn.close();
        }
    }

    private void commitIfActive(Connection conn) {
        if (conn.isInTransaction()) {
            conn.commit();
        }
    }

    private void rollbackIfActive(Connection conn) {
        if (conn.isInTransaction()) {
            conn.rollback();
        }
    }
}
