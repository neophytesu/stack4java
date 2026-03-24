package http;

import http.base.HttpRequest;
import http.base.HttpResponse;
import http.servlet.SuccessServlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HelloTest {
    static void main()  {
        HttpServer server = new HttpServer(8080);
        server.addFilter((request, response, filterChain) -> filterChain.doFilter(request, response));
        server.addServlet("/hello", new SuccessServlet() {
            @Override
            public void service(HttpRequest request, HttpResponse response) throws IOException {
                super.service(request, response);
                response.setBody("玉汝于成！".getBytes(StandardCharsets.UTF_8));
                response.getHeaders().put("Content-Type", "text/plain;charset=utf-8");
                response.getHeaders().put("Content-Length", String.valueOf(response.getBody().length));
            }
        },"success1");
        server.addServlet("/su", new SuccessServlet() {
            @Override
            public void service(HttpRequest request, HttpResponse response) throws IOException {
                request.getRequestDispatcher("/static").forward(request, response);
            }
        },"success2");
        server.start();
    }
}
