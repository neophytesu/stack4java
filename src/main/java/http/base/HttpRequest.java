package http.base;

import http.HttpServer;
import http.HttpServletContext;
import http.dispatch.RequestDispatcher;
import http.dispatch.RequestDispatcherImpl;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class HttpRequest {

    private HttpServletContext servletContext;

    private String method;
    private String path;
    private String version;

    private Map<String, String> headers;
    private Map<String, String> params = new HashMap<>();
    private Map<String, String> cookies;

    private byte[] body;
    private Map<String, String> bodyParams = new HashMap<>();
    private String bodyJson;

    private HttpSession session;

    private HttpServer httpServer;

    private boolean isForward = false;

    public String getHeadValue(String key) {
        return headers.get(key.toLowerCase());
    }

    public String getParamValue(String key) {
        String p = params.get(key);
        String bp = bodyParams.get(key);
        if (p != null) {
            return p;
        }
        return bp;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return httpServer.getRequestDispatcher(path);
    }
}
