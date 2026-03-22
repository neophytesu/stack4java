package http;

import http.servlet.HttpServlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrlMappingRegistry {

    private final HttpHelper httpHelper = new HttpHelper();

    private final Map<String, HttpServlet> exactMap = new HashMap<>();
    private final Map<String, HttpServlet> prefixMap = new HashMap<>();
    private final Map<String, HttpServlet> suffixMap = new HashMap<>();
    private HttpServlet defaultServlet;


    void addMapping(String pattern, HttpServlet servlet) {
        if (pattern == null || servlet == null) {
            throw new NullPointerException("pattern or servlet is null");
        }
        pattern = httpHelper.normalizePath(pattern);
        if (pattern.equals("/")) {
            defaultServlet = servlet;
            return;
        }
        if (pattern.endsWith("/*")) {
            pattern = pattern.substring(0, pattern.length() - 2);
            prefixMap.put(pattern, servlet);
            return;
        }
        if (pattern.startsWith("*.") && pattern.length() > 2 && !pattern.contains("/")) {
            pattern = pattern.substring(pattern.lastIndexOf("."));
            suffixMap.put(pattern, servlet);
            return;
        }
        if (pattern.contains("*") || pattern.contains("?")) {
            throw new IllegalArgumentException("pattern is illegal");
        }
        exactMap.put(pattern, servlet);
    }

    HttpServlet resolve(String path) {
        path = httpHelper.normalizePath(path);
        if (path.equals("/") || path.isBlank()) {
            return defaultServlet;
        }
        HttpServlet servlet = exactMap.get(path);
        if (servlet != null) {
            return servlet;
        }
        HttpServlet longestMatch = null;
        int l = 0;
        for (String s : prefixMap.keySet()) {
            if (s.length() <= l && l > 0) {
                continue;
            }
            if (path.equals(s) || path.startsWith(s + "/")) {
                l = s.length();
                longestMatch = prefixMap.get(s);
            }
        }
        if (longestMatch != null) {
            return longestMatch;
        }
        for (String s : suffixMap.keySet()) {
            if (path.endsWith(s)) {
                return suffixMap.get(s);
            }
        }
        return defaultServlet;
    }

    List<HttpServlet> getServlets() {
        List<HttpServlet> servlets = new ArrayList<>();
        if (defaultServlet != null) {
            servlets.add(defaultServlet);
        }
        servlets.addAll(exactMap.values());
        servlets.addAll(prefixMap.values());
        servlets.addAll(suffixMap.values());
        return servlets;
    }
}
