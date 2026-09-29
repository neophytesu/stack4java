package mybatis.resolve;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicSql {
    private static final Pattern IF = Pattern.compile("<if test=\"([^\"]+)\">([\\s\\S]*?)</if>");
    private static final Pattern WHERE = Pattern.compile("<where>([\\s\\S]*?)</where>");
    private static final Pattern TEST = Pattern.compile("(\\w+)\\s*(!=|==)\\s*null");
    private static final Pattern LEADING_AND_OR = Pattern.compile("(?i)^\\s*(AND|OR)\\s+");
    private static final Pattern FOREACH = Pattern.compile(
            "<foreach collection=\"(\\w+)\" item=\"(\\w+)\" open=\"([^\"]*)\" separator=\"([^\"]*)\" close=\"([^\"]*)\">([\\s\\S]*?)</foreach>");

    public static String render(String sql, Map<String, Object> named) {
        sql = sql.trim();
        if (!sql.startsWith("<script>") || !sql.endsWith("</script>")) {
            return sql;
        }
        String body = sql.substring("<script>".length(), sql.length() - "</script>".length());
        body = applyIf(body, named);
        body = applyForeach(body, named);
        body = applyWhere(body);
        return body.trim();
    }

    private static String applyForeach(String sql, Map<String, Object> named) {
        return FOREACH.matcher(sql).replaceAll(m -> {
            String collection = m.group(1);
            String item = m.group(2);
            String open = m.group(3);
            String separator = m.group(4);
            String close = m.group(5);
            String body = m.group(6);
            return Matcher.quoteReplacement(expandForeach(named, collection, item, open, separator, close, body));
        });
    }

    private static String expandForeach(Map<String, Object> named, String collection, String item, String open, String separator, String close, String body) {
        if (!named.containsKey(collection)) {
            throw new IllegalArgumentException("foreach 找不到参数：" + collection);
        }
        Object raw = named.get(collection);
        if (!(raw instanceof List<?> list)) {
            throw new IllegalArgumentException(collection + " 不是 List");
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("列表参数不能为空");
        }
        String placeholder = "#{" + item + "}";
        StringBuilder out = new StringBuilder(open);
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                out.append(separator);
            }
            if (!body.contains(placeholder)) {
                throw new IllegalArgumentException("foreach 体缺少 " + placeholder);
            }
            out.append(body.replace(placeholder, "#{" + collection + "[" + i + "]}"));
        }
        out.append(close);
        return out.toString();
    }

    private static String applyWhere(String sql) {
        return WHERE.matcher(sql).replaceAll(m -> {
            String inner = m.group(1).trim();
            if (inner.isEmpty()) {
                return "";
            }
            inner = LEADING_AND_OR.matcher(inner).replaceFirst("");
            return Matcher.quoteReplacement(" WHERE " + inner);
        });
    }

    private static String applyIf(String sql, Map<String, Object> named) {
        return IF.matcher(sql).replaceAll(m -> eval(m.group(1), named) ? Matcher.quoteReplacement(m.group(2)) : "");
    }

    private static boolean eval(String test, Map<String, Object> named) {
        Matcher m = TEST.matcher(test.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("不支持的 test: " + test);
        }
        String name = m.group(1);
        if (!named.containsKey(name)) {
            throw new IllegalArgumentException("test 找不到参数：" + name);
        }
        boolean isNull = named.get(name) == null;
        return "!=".equals(m.group(2)) != isNull;
    }
}
