package http.servlet;

import http.HttpHelper;
import http.base.HttpRequest;
import http.base.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class StaticResourceServlet implements HttpServlet {

    private final NotFoundServlet notFoundServlet = new NotFoundServlet();

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        String relativePath = request.getPath().substring("/static".length());
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        String resourcePath = relativePath;
        if (relativePath.isEmpty() || "/".equals(relativePath)) {
            resourcePath = "static/index.html";
        }
        if (resourcePath.endsWith("/")) {
            resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
        }
        if (resourcePath.contains("..")) {
            notFoundServlet.service(request, response);
            return;
        }
        ClassLoader classLoader = HttpHelper.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                notFoundServlet.service(request, response);
                return;
            }
            response.setBody(inputStream.readAllBytes());
            Map<String, String> headers = new HashMap<>();
            if (resourcePath.lastIndexOf(".") > 0) {
                String contentType = resourcePath.substring(resourcePath.lastIndexOf(".") + 1);
                switch (contentType) {
                    case "html" -> headers.put("Content-Type", "text/html;charset=utf-8");
                    case "json" -> headers.put("Content-Type", "application/json;charset=utf-8");
                    case "text" -> headers.put("Content-Type", "text/plain;charset=utf-8");
                    case "css" -> headers.put("Content-Type", "text/css;charset=utf-8");
                    case "js" -> headers.put("Content-Type", "application/javascript;charset=utf-8");
                    case "ico" -> headers.put("Content-Type", "image/x-icon");
                    default -> headers.put("Content-Type", "application/octet-stream;charset=utf-8");
                }
            } else {
                headers.put("Content-Type", "application/octet-stream;charset=utf-8");
            }

            headers.put("Content-Length", String.valueOf(response.getBody().length));
            response.setHeaders(headers);
            response.setVersion("HTTP/1.1");
            response.setStatusCode(200);
            response.setStatusDesc("OK");
        }
    }
}
