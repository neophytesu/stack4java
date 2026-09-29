package mybatis.mapper;

import jdbc.RowMapper;
import mybatis.annotation.*;
import mybatis.resolve.BoundSql;
import mybatis.resolve.ParamBinder;
import mybatis.session.SqlSession;
import mybatis.session.SqlSessionFactory;
import mybatis.session.SqlSessionHolder;

import java.lang.reflect.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MapperProxy implements InvocationHandler {
    private final SqlSessionFactory sqlSessionFactory;
    private final ConcurrentHashMap<Method, MappedStatement> statements = new ConcurrentHashMap<>();

    public MapperProxy(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        SqlSession bound = SqlSessionHolder.get();
        if (bound != null) {
            return execute(method, args, bound);
        }
        SqlSession session = sqlSessionFactory.openSession();
        try {
            return execute(method, args, session);
        } finally {
            session.close();
        }
    }

    private Object execute(Method method, Object[] args, SqlSession sqlSession) throws IllegalAccessException, InvocationTargetException {
        MappedStatement mappedStatement = statements.computeIfAbsent(method, this::parse);
        String raw = mappedStatement.rawSql();
        if (mappedStatement.sqlProvider() != null) {
            raw = (String) mappedStatement.sqlProvider().invoke(null, args);
        }
        BoundSql bound = ParamBinder.bind(method, raw, args);
        if (!mappedStatement.select()) {
            return sqlSession.update(bound.jdbcSql(), bound.args());
        }
        if (mappedStatement.many()) {
            return sqlSession.selectList(bound.jdbcSql(), bound.args(), mappedStatement.rowMapper());
        }
        return sqlSession.selectOne(bound.jdbcSql(), bound.args(), mappedStatement.rowMapper());
    }

    private MappedStatement parse(Method method) {
        Select selectAnn = method.getAnnotation(Select.class);
        Insert insertAnn = method.getAnnotation(Insert.class);
        Update updateAnn = method.getAnnotation(Update.class);
        Delete deleteAnn = method.getAnnotation(Delete.class);
        SelectProvider selectProviderAnn = method.getAnnotation(SelectProvider.class);
        int commands = countNonNull(selectAnn, insertAnn, updateAnn, deleteAnn, selectProviderAnn);
        if (commands == 0) {
            throw new IllegalStateException(method + " 缺少 SQL 注解");
        }
        if (commands > 1) {
            throw new IllegalStateException(method + " 不能同时标多种 SQL 注解");
        }
        String rawSql = null;
        Method sqlProviderMethod = null;
        boolean select;
        if (selectProviderAnn != null) {
            select = true;
            sqlProviderMethod = resolveProvider(method, selectProviderAnn);
        } else {
            select = selectAnn != null;
            rawSql = selectAnn != null ? selectAnn.value() : insertAnn != null ? insertAnn.value() : updateAnn != null ? updateAnn.value() : deleteAnn.value();
        }
        boolean many = method.getReturnType() == List.class;
        Class<?> mappedType = many ? elementType(method) : method.getReturnType();
        return new MappedStatement(rawSql, sqlProviderMethod, select, many, rowMapper(mappedType));
    }

    private Method resolveProvider(Method mapperMethod, SelectProvider ann) {
        try {
            Method method = ann.type().getDeclaredMethod(ann.method(), mapperMethod.getParameterTypes());
            if (!String.class.equals(method.getReturnType())) {
                throw new IllegalStateException("Provider 必须返回 string：" + method);
            }
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("找不到 Provider：" + ann.type().getName() + "." + ann.method(), e);
        }
    }

    private static int countNonNull(Object... args) {
        int count = 0;
        for (Object arg : args) {
            if (arg != null) {
                count++;
            }
        }
        return count;
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
