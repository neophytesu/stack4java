package mybatis;

import mybatis.annotation.Param;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamBinder {
    private static final Pattern PLACEHOLDER = Pattern.compile("#\\{(\\w+)}");

    public static BoundSql bind(Method method, String sql, Object[] args) {
        Parameter[] parameters = method.getParameters();
        Object[] values = args == null ? new Object[0] : args;
        if (parameters.length != values.length) {
            throw new IllegalArgumentException("参数个数对不上：" + method);
        }
        Map<String, Object> named = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Param param = parameters[i].getAnnotation(Param.class);
            if (param == null) {
                throw new IllegalStateException("参数缺少 @Param：" + method);
            }
            named.put(param.value(), values[i]);
        }
        Matcher matcher = PLACEHOLDER.matcher(sql);
        StringBuilder jdbcSql = new StringBuilder();
        List<Object> jdbcArgs = new ArrayList<>();
        int last = 0;
        while (matcher.find()) {
            jdbcSql.append(sql, last, matcher.start());
            String name = matcher.group(1);
            if (!named.containsKey(name)) {
                throw new IllegalArgumentException("找不到参数：" + name);
            }
            jdbcArgs.add(named.get(name));
            jdbcSql.append('?');
            last = matcher.end();
        }
        jdbcSql.append(sql.substring(last));
        return new BoundSql(jdbcSql.toString(), jdbcArgs.toArray());
    }
}
