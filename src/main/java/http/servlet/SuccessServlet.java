package http.servlet;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.io.IOException;

public class SuccessServlet implements HttpServlet{
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        response.setStatusCode(200);
        response.setStatusDesc("OK");

    }
}
