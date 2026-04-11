package mvc.view.interfaces;

import http.base.HttpRequest;
import http.base.HttpResponse;

import java.io.IOException;
import java.util.Map;

public interface View {
    void render(Map<String, Object> model, HttpRequest request, HttpResponse response) throws IOException;
}
