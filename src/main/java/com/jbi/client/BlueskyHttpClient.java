package com.jbi.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbi.api.*;
import com.jbi.api.EverythingElse.Arbitrary;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;

public final class BlueskyHttpClient {

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

    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String base;
    private final String apiKey;

    private BlueskyHttpClient(String baseUrl, String apiKey) {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.base   = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
    }


    public Envelope<?> call(ApiEndpoint ep, Object body) throws Exception {
        Object requestBody = (body == NoBody.INSTANCE) ? null : body;

        return switch (ep) {
            case STATUS    -> send(ep, requestBody,
                    new TypeReference<Envelope<StatusResponse>>() {});
            case QUEUE_GET -> send(ep, requestBody,
                    new TypeReference<Envelope<QueueGetPayload>>() {});
            default        -> send(ep, requestBody,
                    new TypeReference<Envelope<Arbitrary>>() {});
        };
    }


    public Map<String,Object> send(ApiEndpoint api, Object body) throws Exception {
        return send(api, body,
                new TypeReference<Map<String,Object>>() {});
    }

    public <T> T send(ApiEndpoint api, Object body, Class<T> type) throws Exception {
        HttpRequest req = build(api.endpoint(), body);
        HttpResponse<String> rsp = http.send(req, HttpResponse.BodyHandlers.ofString());
        check(rsp, api);
        return (type == Void.class) ? null : mapper.readValue(rsp.body(), type);
    }

    public <T> T send(ApiEndpoint api, Object body, TypeReference<T> ref) throws Exception {
        HttpRequest req = build(api.endpoint(), body);
        HttpResponse<String> rsp = http.send(req, HttpResponse.BodyHandlers.ofString());
        check(rsp, api);
        return mapper.readValue(rsp.body(), ref);
    }

    private HttpRequest build(Endpoint ep, Object body) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(base + ep.path()))
                .header("Authorization", "ApiKey " + apiKey)
                .header("Content-Type", "application/json");

        return switch (ep.method()) {
            case GET    -> b.GET().build();
            case DELETE -> b.DELETE().build();
            case POST, PUT ->
                    b.method(ep.method().name(),
                                    (body == null)
                                            ? HttpRequest.BodyPublishers.noBody()
                                            : HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                            .build();
        };
    }

    private void check(HttpResponse<String> rsp, ApiEndpoint api) throws Exception {
        if (rsp.statusCode() >= 200 && rsp.statusCode() < 300) {
            // treat {"success":false} as failure
            try {
                BasicResponse br = mapper.readValue(rsp.body(), BasicResponse.class);
                if (!br.success()) throw new RequestFailedException(api, br.msg());
            } catch (Exception ignore) { /* not always enveloped */ }
        } else if (rsp.statusCode() < 500) {
            throw new ClientErrorException(api, rsp);
        } else {
            throw new ServerErrorException(api, rsp);
        }
    }

    public sealed static class BlueskyException extends RuntimeException
            permits ClientErrorException, ServerErrorException, RequestFailedException {
        BlueskyException(String m) { super(m); }
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
