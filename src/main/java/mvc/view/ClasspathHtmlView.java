package mvc.view;

import http.base.HttpRequest;
import http.base.HttpResponse;
import mvc.view.interfaces.View;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class ClasspathHtmlView implements View {
    String classPath;

    public ClasspathHtmlView(String classPath) {
        this.classPath = classPath;
    }

    @Override
    public void render(Map<String, Object> model, HttpRequest request, HttpResponse response) throws IOException {
        try (InputStream resource = ClasspathHtmlView.class.getClassLoader().getResourceAsStream(classPath)) {
            if (resource != null) {
                String temp = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
                for (Map.Entry<String, Object> entry : model.entrySet()) {
                    String slot = entry.getKey();
                    String data = Objects.toString(entry.getValue(), "");
                    temp = temp.replace("{{" + slot + "}}", data);
                }
                response.setStatusCode(200);
                response.setStatusDesc("OK");
                response.setBody(temp.getBytes(StandardCharsets.UTF_8));
                response.getHeaders().put("Content-Type", "text/html;charset=utf-8");
                response.getHeaders().put("Content-Length", String.valueOf(response.getBody().length));
            } else {
                throw new IllegalStateException("不存在的模版路径：" + classPath);
            }
        }
    }
}
