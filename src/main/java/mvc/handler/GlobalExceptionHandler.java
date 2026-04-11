package mvc.handler;

import http.base.HttpRequest;
import http.base.HttpResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class GlobalExceptionHandler {
    ObjectMapper objectMapper = new ObjectMapper();

    public void resolve(Throwable t, HttpRequest request, HttpResponse response) throws IOException {
        if (wantsJson(request)) {
            writeJsonError(response, t);
        } else {
            request.getAttributes().put("exception", t);
            request.getRequestDispatcher("/error").forward(request, response);
        }
    }

    private boolean wantsJson(HttpRequest request) {
        String accept = request.getHeadValue("Accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }
        if (request.getPath().startsWith("/api")) {
            return true;
        }
        return Boolean.TRUE.equals(request.getAttributes().get("mvc.apiError"));
    }

    private void writeJsonError(HttpResponse response, Throwable t) {
        response.setStatusCode(500);
        response.getHeaders().put("Content-Type", "application/json;charset=utf-8");
        response.setBody(objectMapper.writeValueAsBytes(Map.of("error", Objects.toString(t.getMessage(), "Unknown error"))));
        response.getHeaders().put("Content-Length", String.valueOf(response.getBody().length));
    }
}
