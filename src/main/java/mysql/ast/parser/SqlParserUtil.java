package mysql.ast.parser;

import java.util.ArrayList;
import java.util.List;

public class SqlParserUtil {

    public static String between(String s, String front, String behind) {
        String lowerSql = s.toUpperCase();
        int startIndex = lowerSql.indexOf(front);
        int endIndex = lowerSql.indexOf(behind);
        String missing = (startIndex == -1 && endIndex == -1) ? front + "和" + behind : (startIndex == -1 ? front : behind);
        if (startIndex == -1 || endIndex == -1) {
            throw new SqlParseException("缺少" + missing);
        }
        if (startIndex > endIndex) {
            throw new SqlParseException(behind + "不能在" + front + "之前");
        }
        return s.substring(startIndex + front.length(), endIndex);
    }

    public static String before(String s, String behind) {
        int index = s.indexOf(behind);
        if (index == -1) {
            throw new SqlParseException("缺少" + behind);
        }
        return s.substring(0, index);
    }


    public static String after(String s, String front) {
        int index = s.toLowerCase().indexOf(front.toLowerCase());
        if (index == -1) {
            throw new SqlParseException("缺少" + front);
        }
        return s.substring(index + front.length());
    }

    public static Object parseLiteral(String token) {
        token = token.trim();
        if (token.startsWith("'") && token.endsWith("'")) {
            token = token.substring(1, token.length() - 1);
            return token.replace("''", "'");

        }
        if ("true".equalsIgnoreCase(token) || "false".equalsIgnoreCase(token)) {
            return Boolean.parseBoolean(token);
        }
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new SqlParseException("不支持的值：" + token);
        }

    }

    public static String[] splitEquals(String s) {
        if (!s.contains("=")) {
            throw new SqlParseException("缺少'='：" + s);
        }
        int i = s.indexOf("=");
        if (i == s.length() - 1) {
            throw new SqlParseException("'='后面缺少值：" + s);
        }
        return new String[]{s.substring(0, i), s.substring(i + 1)};
    }

    public static String preprocess(String s) {
        s = s.trim();
        if (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        return s.replaceAll("\\s+", " ");
    }

    public static String firstKeyword(String s) {
        return s.split(" ", 2)[0];
    }

    public static List<Object> parseValueList(String insideParens) {
        List<Object> values = new ArrayList<>();
        for (String part : splitByCommaRespectQuotes(insideParens)) {
            values.add(parseLiteral(part));
        }
        return values;
    }

    public static List<String> splitByCommaRespectQuotes(String s) {
        List<String> parts = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                if (inQuote && i + 1 < s.length() && s.charAt(i + 1) == '\'') {
                    buf.append("''");
                    i++;
                    continue;
                }
                inQuote = !inQuote;
                buf.append(c);
                continue;
            }
            if (c == ',' && !inQuote) {
                parts.add(buf.toString().trim());
                buf.setLength(0);
                continue;
            }
            buf.append(c);
        }
        if (inQuote) {
            throw new SqlParseException("字符串引号未闭环：" + s);
        }
        if (!buf.isEmpty()) {
            parts.add(buf.toString().trim());
        }
        return parts;
    }

    public static String betweenParentheses(String s, String front) {
        String lowerSql = s.toLowerCase();
        int index = lowerSql.indexOf(front.toLowerCase());
        if (index == -1) {
            throw new SqlParseException("缺少" + front);
        }
        String valuesPart = s.substring(index + front.length());
        int open = valuesPart.indexOf("(");
        int close = valuesPart.indexOf(")");
        if (open == -1 || close == -1 || close <= open) {
            throw new SqlParseException("VALUES 缺少括号");
        }
        return valuesPart.substring(open + 1, close);
    }
}
