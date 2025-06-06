package com.jbi.client;


import com.jbi.api.StatusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class BlueskyHttpClient {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String apiKey;

    public BlueskyHttpClient(String baseUrl, String apiKey) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    private HttpRequest.Builder requestBuilder(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "ApiKey " + apiKey)
                .header("Content-Type", "application/json");
    }

    public StatusResponse getStatus() throws Exception {
        HttpRequest request = requestBuilder("/api/status")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(response.body(), StatusResponse.class);
    }

    public void openEnvironment() throws Exception {
        HttpRequest request = requestBuilder("/api/environment/open")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        client.send(request, HttpResponse.BodyHandlers.discarding());
    }

    public void closeEnvironment() throws Exception {
        HttpRequest request = requestBuilder("/api/environment/close")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        client.send(request, HttpResponse.BodyHandlers.discarding());
    }
}

