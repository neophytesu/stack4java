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
    private static final Pattern PLACEHOLDER = Pattern.compile("#\\{([\\w.]+)(?:\\[(\\d+)])?}");
    private static final Pattern DOLLAR = Pattern.compile("\\$\\{([\\w.]+)}");

    public static BoundSql bind(Method method, String sql, Object[] args) {
        Parameter[] parameters = method.getParameters();
        Object[] values = args == null ? new Object[0] : args;
        if (parameters.length != values.length) {
            throw new IllegalArgumentException("参数个数对不上：" + method);
        }
        Map<String, Object> named = namedArgs(method, values);
        sql = substituteDollar(sql, named);
        Matcher matcher = PLACEHOLDER.matcher(sql);
        StringBuilder jdbcSql = new StringBuilder();
        List<Object> jdbcArgs = new ArrayList<>();
        int last = 0;
        while (matcher.find()) {
            String path = matcher.group(1);
            String index = matcher.group(2);
            Object value = index == null ? resolve(named, path) : atIndex(named, path, Integer.parseInt(index));

            jdbcSql.append(sql, last, matcher.start());
            if (index == null && value instanceof List<?> list) {
                appendList(jdbcSql, jdbcArgs, list);
            } else {
                jdbcArgs.add(value);
                jdbcSql.append('?');
            }
            last = matcher.end();
        }
        jdbcSql.append(sql.substring(last));
        return new BoundSql(jdbcSql.toString(), jdbcArgs.toArray());
    }

    private static String substituteDollar(String sql, Map<String, Object> named) {
        Matcher matcher = DOLLAR.matcher(sql);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            Object value = resolve(named, matcher.group(1));
            if (value == null) {
                throw new IllegalArgumentException("${} 不能为 null：" + matcher.group(1));
            }
            out.append(sql, last, matcher.start());
            out.append(value);
            last = matcher.end();
        }
        out.append(sql.substring(last));
        return out.toString();
    }

    private static void appendList(StringBuilder jdbcSql, List<Object> jdbcArgs, List<?> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("列表参数不能为空");
        }
        jdbcSql.append("?");
        jdbcArgs.addAll(list);
        jdbcSql.repeat(",?", Math.max(0, list.size() - 1));
    }

    private static Object atIndex(Map<String, Object> named, String path, int index) {
        if (path.contains(".")) {
            throw new IllegalArgumentException("这一版不支持 #{a.b[0]}：" + path);
        }
        if (!named.containsKey(path)) {
            throw new IllegalArgumentException("找不到参数：" + path);
        }
        Object raw = named.get(path);
        if (!(raw instanceof List<?> list)) {
            throw new IllegalArgumentException(path + " 不是 List");
        }
        if (index < 0 || index >= list.size()) {
            throw new IllegalArgumentException(path + " 下标越界：" + index);
        }
        return list.get(index);
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

    public static Map<String, Object> namedArgs(Method method, Object[] values) {
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
