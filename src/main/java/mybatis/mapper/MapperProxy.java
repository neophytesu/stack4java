package mybatis.mapper;

import jdbc.RowMapper;
import mybatis.resolve.BoundSql;
import mybatis.resolve.ParamBinder;
import mybatis.session.SqlSession;
import mybatis.annotation.Delete;
import mybatis.annotation.Insert;
import mybatis.annotation.Select;
import mybatis.annotation.Update;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MapperProxy implements InvocationHandler {
    private final SqlSession sqlSession;
    private final ConcurrentHashMap<Method, MappedStatement> statements = new ConcurrentHashMap<>();

    public MapperProxy(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        MappedStatement mappedStatement = statements.computeIfAbsent(method, this::parse);
        BoundSql bound = ParamBinder.bind(method, mappedStatement.rawSql(), args);
        if (!mappedStatement.select()) {
            return sqlSession.update(bound.jdbcSql(), bound.args());
        }
        if (mappedStatement.many()) {
            return sqlSession.selectList(bound.jdbcSql(), bound.args(), mappedStatement.rowMapper());
        }
        return sqlSession.selectOne(bound.jdbcSql(), bound.args(), mappedStatement.rowMapper());
    }

    private MappedStatement parse(Method method) {
        String raw = sqlOf(method);
        boolean select = method.getAnnotation(Select.class) != null;
        boolean many = method.getReturnType() == List.class;
        Class<?> mappedType = many ? elementType(method) : method.getReturnType();
        return new MappedStatement(raw, select, many, rowMapper(mappedType));
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
        if (isScalar(returnType)) {
            return new ScalarRowMapper<>(returnType);
        }
        return new PojoRowMapper<>(returnType);
    }

    private boolean isScalar(Class<?> returnType) {
        return returnType.isPrimitive() || returnType == String.class || returnType == Integer.class || returnType == Long.class;
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
