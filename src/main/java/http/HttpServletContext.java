package http;

import java.util.concurrent.ConcurrentHashMap;

public class HttpServletContext {

    private final ConcurrentHashMap<String, String> initParams = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<>();

    public String getServletContextName() {
        return initParams.get("appName") == null ? "" : initParams.get("appName");
    }

    public String getServletContextPath() {
        return initParams.get("path") == null ? "" : initParams.get("path");
    }

    public void addInitParam(String name, String value) {
        initParams.put(name, value);
    }

    public String getInitParameter(String name) {
        return initParams.get(name);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void log(String msg) {
        System.out.println(msg);
    }
}
