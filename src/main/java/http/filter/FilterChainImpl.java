package http.filter;

import http.base.HttpRequest;
import http.base.HttpResponse;
import http.servlet.HttpServlet;

import java.io.IOException;
import java.util.List;

public class FilterChainImpl implements FilterChain {

    private List<HttpFilter> filters;
    private HttpServlet servlet;
    private int index;

    public FilterChainImpl(List<HttpFilter> filters, HttpServlet servlet, int index) {
        this.filters = filters;
        this.servlet = servlet;
        this.index = index;
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponse response) throws IOException {
        if (index < filters.size()) {
            HttpFilter currentFilter = filters.get(index);
            FilterChainImpl nextFilterChain = new FilterChainImpl(filters, servlet, index + 1);
            currentFilter.doFilter(request, response, nextFilterChain);
        } else {
            servlet.service(request, response);
        }
    }
}
