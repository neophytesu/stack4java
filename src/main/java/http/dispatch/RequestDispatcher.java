package http.dispatch;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.io.IOException;

public interface RequestDispatcher {
    void forward(HttpRequest request, HttpResponse response) throws IOException;
}
