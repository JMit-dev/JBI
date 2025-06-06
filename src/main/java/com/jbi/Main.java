package com.jbi;

import com.jbi.client.BlueskyHttpClient;
import com.jbi.api.StatusResponse;

public class Main {
    public static void main(String[] args) {
        String baseUrl = "http://localhost:60610";
        String apiKey = System.getenv("BLUESKY_API_KEY");
        if (apiKey == null) {
            throw new IllegalStateException("BLUESKY_API_KEY environment variable is not set.");
        }

        BlueskyHttpClient client = new BlueskyHttpClient(baseUrl, apiKey);

        try {
            StatusResponse status = client.getStatus();
            System.out.println("Manager State: " + status.managerState());

            client.openEnvironment();
            System.out.println("Environment opened.");

            client.closeEnvironment();
            System.out.println("Environment closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
