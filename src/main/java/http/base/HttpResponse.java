package http.base;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class HttpResponse {
    private String version;
    private int statusCode;
    private String statusDesc;

    private Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    private Map<String, String> cookies = new HashMap<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(version).append(" ").append(statusCode).append(" ").append(statusDesc).append("\r\n");
        headers.forEach((k, v) -> sb.append(k.trim()).append(": ").append(v.trim()).append("\r\n"));
        cookies.forEach((k, v) -> sb.append("Set-Cookie: ").append(k).append("=").append(v).append("\r\n"));
        sb.append("\r\n");
        return sb.toString();
    }
}
