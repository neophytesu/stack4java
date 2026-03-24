package http;

import lombok.Getter;

import java.util.HashMap;


public class HttpServletConfig {
    @Getter
    private final HttpServletContext servletContext;
    private final HashMap<String, String> initParams;
    @Getter
    private final String servletName;

    public HttpServletConfig(HttpServletContext servletContext, HashMap<String, String> initParams, String servletName) {
        this.servletContext = servletContext;
        this.initParams = initParams;
        this.servletName = servletName;
    }

    public String getInitParameter(String name) {
        return initParams.get(name);
    }
}
