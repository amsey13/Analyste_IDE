package com.example.backend.modules.analysis.exporter;

import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
@Component
public class ClientHttp {

    private final String method;
    private Map<String, String> headers;
    private String body;
    private final int timeout;






    public ClientHttp(String method, Map<String, String> headers, String body, int timeout) {
        this.method = method;
        this.headers = headers;
        this.body = body;
        this.timeout = timeout;
    }

    public ClientHttp() {
        this.timeout = 20000;
        this.method = "POST";
    }


    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * The `execute` function sends an HTTP request to a specified URL with configurable method,
     * headers, and body, and returns the response status code, headers, and body.
     * 
     * @param cible The `cible` parameter in the `execute` method is a string representing the target
     * URL to which the HTTP request will be sent. It is used to create a URL object for the target
     * endpoint.
     * @return An `HttpResponse` object is being returned, which contains the HTTP status code,
     * response headers, and response body from the HTTP request.
     */
    public HttpResponse execute(String cible) throws IOException {

        URL url = URI.create(cible).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Time configuration a little bit longer cause the request is heavy
        conn.setRequestMethod(this.method);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(this.timeout);


        if (this.headers != null) {
            for (Map.Entry<String, String> entry : this.headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }


        if (this.body != null && !this.body.isEmpty() &&
                (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("PATCH"))) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(this.body.getBytes(StandardCharsets.UTF_8));
            }
        }


        int status = conn.getResponseCode();
        Map<String, List<String>> respHeaders = conn.getHeaderFields();

        InputStream stream = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (stream != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
        }
        String respBody = sb.toString();

        conn.disconnect();


        return new HttpResponse(status, respHeaders, respBody);
    }


    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
