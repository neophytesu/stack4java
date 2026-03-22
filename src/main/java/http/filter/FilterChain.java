package http.filter;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.io.IOException;

public interface FilterChain {
    void doFilter(HttpRequest request, HttpResponse response) throws IOException;
}
