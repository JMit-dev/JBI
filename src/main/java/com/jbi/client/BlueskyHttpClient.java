package com.jbi.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbi.api.BasicResponse;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;

public final class BlueskyHttpClient {

    /* ---------- singleton bootstrapping ---------- */
    private static volatile BlueskyHttpClient INSTANCE;
    public static void initialize(String baseUrl, String apiKey) {
        if (INSTANCE == null) {
            synchronized (BlueskyHttpClient.class) {
                if (INSTANCE == null) INSTANCE = new BlueskyHttpClient(baseUrl, apiKey);
            }
        }
    }
    public static BlueskyHttpClient get() {
        if (INSTANCE == null) throw new IllegalStateException("BlueskyHttpClient not initialised");
        return INSTANCE;
    }

    /* ---------- instance ---------- */
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String base;
    private final String apiKey;

    private BlueskyHttpClient(String baseUrl, String apiKey) {
        this.http   = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.base   = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
    }

    /* ---------- public façade ---------- */

    // strongly-typed convenience:
    public com.jbi.api.StatusResponse status() throws Exception {
        return send(ApiEndpoint.STATUS, null, com.jbi.api.StatusResponse.class);
    }
    public void environmentOpen()  throws Exception { noContent(ApiEndpoint.ENVIRONMENT_OPEN); }
    public void environmentClose() throws Exception { noContent(ApiEndpoint.ENVIRONMENT_CLOSE); }

    // generic fallback (record-heavy callers can use it directly)
    public <T> T send(ApiEndpoint api, Object body, Class<T> type) throws Exception {
        HttpRequest req = build(api.endpoint(), body);
        HttpResponse<String> rsp = http.send(req, HttpResponse.BodyHandlers.ofString());
        check(rsp, api);
        // shortcut: some endpoints have no JSON body (“OK” string). Handle here
        if (type == Void.class) return null;
        return mapper.readValue(rsp.body(), type);
    }

    /** For endpoints that only say `{"success": true}` or empty body */
    private void noContent(ApiEndpoint api) throws Exception {
        send(api, null, Void.class);
    }

    // Map / JsonNode variant if you need totally dynamic access
    public Map<String,Object> send(ApiEndpoint api, Object body) throws Exception {
        HttpRequest req = build(api.endpoint(), body);
        HttpResponse<String> rsp = http.send(req, HttpResponse.BodyHandlers.ofString());
        check(rsp, api);
        return mapper.readValue(rsp.body(), new TypeReference<>() {});
    }

    /* ---------- helpers ---------- */

    private HttpRequest build(Endpoint ep, Object body) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(base + ep.path()))
                .header("Authorization", "ApiKey " + apiKey)
                .header("Content-Type", "application/json");

        return switch (ep.method()) {
            case GET    -> b.GET().build();
            case DELETE -> b.DELETE().build();
            case POST, PUT -> {
                HttpRequest.BodyPublisher pub =
                        (body == null)
                                ? HttpRequest.BodyPublishers.noBody()
                                : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body));
                yield b.method(ep.method().name(), pub).build();
            }
        };
    }

    private void check(HttpResponse<String> rsp, ApiEndpoint api) throws Exception {
        if (rsp.statusCode() >= 200 && rsp.statusCode() < 300) {
            // if JSON envelope has {'success': false}, treat as failure too
            try {
                BasicResponse br = mapper.readValue(rsp.body(), BasicResponse.class);
                if (!br.success()) throw new RequestFailedException(api, br.msg());
            } catch (Exception ignore) { /* not every endpoint wraps */ }
        } else if (rsp.statusCode() < 500) {
            throw new ClientErrorException(api, rsp);
        } else {
            throw new ServerErrorException(api, rsp);
        }
    }

    /* ---------- tiny domain exceptions ---------- */
    public sealed static class BlueskyException extends RuntimeException
            permits ClientErrorException, ServerErrorException, RequestFailedException {
        BlueskyException(String msg) { super(msg); }
    }
    public static final class ClientErrorException extends BlueskyException {
        ClientErrorException(ApiEndpoint api, HttpResponse<?> rsp) {
            super(api + " → " + rsp.statusCode() + " " + rsp.body());
        }
    }
    public static final class ServerErrorException extends BlueskyException {
        ServerErrorException(ApiEndpoint api, HttpResponse<?> rsp) {
            super(api + " → server error " + rsp.statusCode());
        }
    }
    public static final class RequestFailedException extends BlueskyException {
        RequestFailedException(ApiEndpoint api, String msg) {
            super(api + " → " + msg);
        }
    }
}
