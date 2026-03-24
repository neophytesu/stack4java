package mvc.handler;

import http.base.HttpRequest;
import http.base.HttpResponse;

public interface Handler {
    void handle(HttpRequest request, HttpResponse response) throws Exception;

}
