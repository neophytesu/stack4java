package mybatis;

import jdbc.RowMapper;
import mybatis.annotation.Delete;
import mybatis.annotation.Insert;
import mybatis.annotation.Select;
import mybatis.annotation.Update;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class MapperProxy implements InvocationHandler {
    private final SqlSession sqlSession;

    public MapperProxy(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        String raw = sqlOf(method);
        BoundSql bound = ParamBinder.bind(method, raw, args);
        Object[] param = bound.args();
        String sql = bound.jdbcSql();
        if (method.getAnnotation(Select.class) != null) {
            Class<?> returnType = method.getReturnType();
            if (returnType == List.class) {
                return sqlSession.getJdbcTemplate().query(sql, param, rowMapper(elementType(method)));
            }
            return sqlSession.getJdbcTemplate().queryForObject(sql, param, rowMapper(returnType));
        }
        return sqlSession.getJdbcTemplate().update(sql, param);
    }

    private String sqlOf(Method method) {
        Select select = method.getAnnotation(Select.class);
        if (select != null) {
            return select.value();
        }
        Insert insert = method.getAnnotation(Insert.class);
        if (insert != null) {
            return insert.value();
        }
        Update update = method.getAnnotation(Update.class);
        if (update != null) {
            return update.value();
        }
        Delete delete = method.getAnnotation(Delete.class);
        if (delete != null) {
            return delete.value();
        }
        throw new IllegalStateException(method + " 缺少 @Select/@Insert/@Update/@Delete");
    }

    private RowMapper<?> rowMapper(Class<?> returnType) {
        return new PojoRowMapper<>(returnType);
    }

    private Class<?> elementType(Method method) {
        Type type = method.getGenericReturnType();
        if (type instanceof ParameterizedType pt) {
            Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> c) {
                return c;
            }
        }
        throw new IllegalStateException("List 必须写清泛型：" + method);
    }
}
