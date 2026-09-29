package mybatis.resolve;

import mybatis.annotation.Param;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamBinder {
    private static final Pattern PLACEHOLDER = Pattern.compile("#\\{([\\w.]+)}");

    public static BoundSql bind(Method method, String sql, Object[] args) {
        Parameter[] parameters = method.getParameters();
        Object[] values = args == null ? new Object[0] : args;
        if (parameters.length != values.length) {
            throw new IllegalArgumentException("参数个数对不上：" + method);
        }
        Map<String, Object> named = namedArgs(method, values);
        Matcher matcher = PLACEHOLDER.matcher(sql);
        StringBuilder jdbcSql = new StringBuilder();
        List<Object> jdbcArgs = new ArrayList<>();
        int last = 0;
        while (matcher.find()) {
            jdbcSql.append(sql, last, matcher.start());
            jdbcArgs.add(resolve(named, matcher.group(1)));
            jdbcSql.append('?');
            last = matcher.end();
        }
        jdbcSql.append(sql.substring(last));
        return new BoundSql(jdbcSql.toString(), jdbcArgs.toArray());
    }

    private static Object resolve(Map<String, Object> named, String path) {
        String[] parts = path.split("\\.");
        if (!named.containsKey(parts[0])) {
            throw new IllegalArgumentException("找不到参数：" + path);
        }
        Object cur = named.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (cur == null) {
                return null;
            }
            cur = property(cur, parts[i]);
        }
        return cur;
    }

    private static Object property(Object target, String name) {
        if (!target.getClass().isRecord()) {
            throw new IllegalStateException("这一版只读 record 属性：" + target.getClass());
        }
        for (RecordComponent c : target.getClass().getRecordComponents()) {
            if (c.getName().equals(name)) {
                try {
                    return c.getAccessor().invoke(target);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        throw new IllegalArgumentException("没有属性：" + name);
    }

    private static Map<String, Object> namedArgs(Method method, Object[] values) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return Map.of();
        }
        boolean allParam = true;
        for (Parameter p : parameters) {
            if (p.getAnnotation(Param.class) == null) {
                allParam = false;
                break;
            }
        }
        if (allParam) {
            Map<String, Object> named = new HashMap<>();
            for (int i = 0; i < parameters.length; i++) {
                named.put(parameters[i].getAnnotation(Param.class).value(), values[i]);
            }
            return named;
        }
        if (parameters.length == 1 && values[0] != null && values[0].getClass().isRecord()) {
            return fromRecord(values[0]);
        }
        throw new IllegalStateException("参数缺少 @Param，且不是单 record：" + method);
    }

    private static Map<String, Object> fromRecord(Object record) {
        Map<String, Object> named = new HashMap<>();
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            try {
                named.put(component.getName(), component.getAccessor().invoke(record));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return named;
    }
}
