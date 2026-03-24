package http.servlet;

import http.HttpServletConfig;
import http.base.HttpRequest;
import http.base.HttpResponse;

import java.io.IOException;

public interface HttpServlet {

    void service(HttpRequest request, HttpResponse response) throws IOException;

    default void init(HttpServletConfig config) {
    }

    default void destroy() {
    }
}
