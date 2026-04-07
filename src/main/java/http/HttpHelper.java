package http;

import http.base.HttpRequest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HttpHelper {

    public String normalizePath(String path) {
        if (Objects.isNull(path) || "/".equals(path) || path.isEmpty()) {
            return "/";
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public HttpRequest parseRequest(BufferedInputStream input) throws IOException {
        HttpRequest httpRequest = new HttpRequest();
        String requestLine = readLine(input);
        if (requestLine == null) {
            return null;
        }
        String[] requestParts = requestLine.split(" ");
        httpRequest.setMethod(requestParts[0]);
        parseParams(URLDecoder.decode(requestParts[1], StandardCharsets.UTF_8), httpRequest);
        httpRequest.setVersion(requestParts[2]);
        Map<String, String> headers = new HashMap<>();
        while (true) {
            String headerLine = readLine(input);
            if (headerLine == null) {
                return null;
            }
            if (Objects.equals(headerLine, "")) {
                break;
            }
            int index = headerLine.indexOf(":");
            if (index == -1) {
                continue;
            }
            String key = headerLine.substring(0, index).trim();
            String value = headerLine.substring(index + 1).trim();
            headers.put(key.toLowerCase(), value.toLowerCase());
        }
        httpRequest.setHeaders(headers);
        parseCookies(httpRequest);
        if (httpRequest.getMethod().equals("POST")) {
            String contentLength = headers.get("content-length");
            int len;
            if (contentLength == null) {
                len = 0;
            } else {
                len = Integer.parseInt(contentLength);
            }
            if (len > 0) {
                byte[] body = new byte[len];
                int offset = 0;
                while (offset < len) {
                    int read = input.read(body, offset, len - offset);
                    if (read == -1) {
                        break;
                    }
                    offset += read;
                }
                httpRequest.setBody(body);
                parseBody(httpRequest);
            } else {
                httpRequest.setBody(new byte[0]);
            }
        }
        return httpRequest;
    }

    private void parseBody(HttpRequest request) {
        String contentType = request.getHeadValue("Content-Type");
        String body;
        if (request.getBody() == null || request.getBody().length == 0) {
            return;
        }
        if (contentType == null) {
            return;
        } else {
            body = new String(request.getBody(), StandardCharsets.UTF_8);
        }
        if (contentType.startsWith("application/x-www-form-urlencoded")) {
            request.setBodyParams(new HashMap<>());
            String[] kv = body.split("&");
            for (String kvStr : kv) {
                String[] kvSplit = kvStr.split("=", 2);
                String key = URLDecoder.decode(kvSplit[0].trim(), StandardCharsets.UTF_8);
                String value = kvSplit.length > 1 ? URLDecoder.decode(kvSplit[1].trim(), StandardCharsets.UTF_8) : "";
                request.getBodyParams().put(key, value);
            }
        }
        if (contentType.startsWith("application/json")) {
            request.setBodyJson(body);
        }
    }

    private void parseParams(String path, HttpRequest request) {
        int index = path.indexOf("?");
        if (index == -1) {
            request.setPath(path);
            request.setParams(new HashMap<>());
        } else {
            request.setPath(path.substring(0, index));
            String[] param = path.substring(index + 1).split("&");
            Map<String, String> map = new HashMap<>();
            for (String s : param) {
                if (s.isEmpty()) {
                    continue;
                }
                String[] kv = s.split("=", 2);
                if (kv.length == 2) {
                    map.put(kv[0].trim(), kv[1].trim());
                } else if (kv.length == 1) {
                    map.put(kv[0].trim(), "");
                }
            }
            request.setParams(map);
        }
    }

    private void parseCookies(HttpRequest request) {
        String originalCookie = request.getHeadValue("Cookie");
        if (Objects.isNull(originalCookie) || originalCookie.isEmpty()) {
            request.setCookies(new HashMap<>());
        } else {
            Map<String, String> cookies = new HashMap<>();
            for (String s : originalCookie.split(";")) {
                String[] kvSplit = s.split("=", 2);
                String key = kvSplit[0].trim();
                String value = kvSplit.length > 1 ? kvSplit[1].trim() : "";
                cookies.put(key, value);
            }
            request.setCookies(cookies);
        }
    }

    private String readLine(BufferedInputStream input) throws IOException {
        byte[] buffer = new byte[1024];
        int index = 0;
        while (true) {
            int ch = input.read();
            if (ch == -1) {
                if (index == 0) {
                    return null;
                }
                break;
            }
            if (ch == '\n') {
                break;
            }
            if (index == buffer.length) {
                byte[] newBuffer = new byte[buffer.length << 1];
                System.arraycopy(buffer, 0, newBuffer, 0, index);
                buffer = newBuffer;
            }
            buffer[index] = (byte) ch;
            index++;
        }
        if (index > 0 && buffer[index - 1] == '\r') {
            index--;
        }
        return new String(buffer, 0, index, StandardCharsets.UTF_8);
    }

    public boolean checkClose(HttpRequest httpRequest) {
        String closeHeader = httpRequest.getHeadValue("Connection");
        if (closeHeader == null || closeHeader.isEmpty()) {
            return false;
        }
        return closeHeader.trim().equals("close");
    }
}
