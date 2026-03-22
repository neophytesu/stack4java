package http.dispatch;

import http.HttpServer;
import http.base.HttpRequest;
import http.base.HttpResponse;

import java.io.IOException;
import java.util.HashMap;

public class RequestDispatcherImpl implements RequestDispatcher {

    private final HttpServer server;

    private final String targetPath;

    public RequestDispatcherImpl(HttpServer server, String targetPath) {
        this.server = server;
        this.targetPath = targetPath;
    }

    @Override
    public void forward(HttpRequest request, HttpResponse response) throws IOException {
        if (request.isForward()) {
            throw new IllegalStateException("request is already forward");
        }
        request.setForward(true);
        response.setBody(new byte[0]);
        response.setHeaders(new HashMap<>());
        response.setStatusCode(200);
        response.setStatusDesc("OK");
        String originalPath = request.getPath();
        request.setPath(targetPath);
        request.getHeaders().put("OriginPath", originalPath);
        server.dispatch(targetPath, request, response);
    }
}
