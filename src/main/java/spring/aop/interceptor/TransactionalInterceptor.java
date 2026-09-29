package spring.aop.interceptor;

import jdbc.Connection;
import jdbc.DataSource;
import jdbc.support.ConnectionHolder;
import mybatis.session.SqlSession;
import mybatis.session.SqlSessionFactory;
import mybatis.session.SqlSessionHolder;

import java.lang.reflect.InvocationTargetException;

public class TransactionalInterceptor implements MethodInterceptor {
    private final DataSource dataSource;
    private final SqlSessionFactory sqlSessionFactory;

    public TransactionalInterceptor(DataSource dataSource, SqlSessionFactory sqlSessionFactory) {
        this.dataSource = dataSource;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public Object invoke(MethodInvocation invocation) {
        Connection conn = dataSource.getConnection();
        ConnectionHolder.bind(conn);
        conn.setAutoCommit(false);
        SqlSession session = sqlSessionFactory.openSession();
        SqlSessionHolder.bind(session);
        try {
            Object result = invocation.proceed();
            commitIfActive(conn);
            return result;
        } catch (Exception e) {
            rollbackIfActive(conn);
            if (e instanceof InvocationTargetException t) {
                throw new RuntimeException(t.getTargetException());
            }
            throw new RuntimeException(e);
        } finally {
            session.close();
            SqlSessionHolder.clear();
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
