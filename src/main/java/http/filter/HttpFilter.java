package http.filter;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.io.IOException;

public interface HttpFilter {
    void doFilter(HttpRequest request, HttpResponse response, FilterChain filterChain) throws IOException;
}
