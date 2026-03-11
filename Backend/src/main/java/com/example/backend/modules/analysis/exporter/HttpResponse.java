package com.example.backend.modules.analysis.exporter;

import java.util.List;
import java.util.Map;

public class HttpResponse {

    private final int code;
    private final Map<String, List<String>> headers;
    private final String body;

    public HttpResponse(int code, Map<String, List<String>> headers, String body) {
        this.code = code;
        this.headers = headers;
        this.body = body;
    }

    public int getCode() {
        return code;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public boolean isSuccess() {
        return code >= 200 && code < 400;
    }

    public String getContentType() {
        List<String> contentType = headers.get("Content-Type");
        return (contentType != null && !contentType.isEmpty()) ? contentType.get(0) : null;
    }
}
