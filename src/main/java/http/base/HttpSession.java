package http.base;

import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;

@Data
public class HttpSession {
    private String sessionId;

    private ConcurrentHashMap<String, String> attributes = new ConcurrentHashMap<>();

    private long lastAccessTime = System.currentTimeMillis();

    private long defaultMaxInactiveIntervalMillis = 7 * 24 * 60 * 60 * 1000;

    public String getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    public void setAttribute(String attributeName, String attributeValue) {
        attributes.put(attributeName, attributeValue);
    }

    public void removeAttribute(String attributeName) {
        attributes.remove(attributeName);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - lastAccessTime > defaultMaxInactiveIntervalMillis;
    }

    public void touch() {
        lastAccessTime = System.currentTimeMillis();
    }
}
