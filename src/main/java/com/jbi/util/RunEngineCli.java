package com.jbi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbi.client.*;
import com.jbi.client.BlueskyHttpClient.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public final class RunEngineCli {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static void usage() {
        System.out.println("""
            Bluesky CLI
            Usage:
              java … BlueskyCli list
              java … BlueskyCli <ENDPOINT> [body]

              list            Print all ApiEndpoint constants
              ENDPOINT        Any constant from ApiEndpoint (case-insensitive)
              body            JSON string
                              @file.json  – read JSON from file
                              @-          – read JSON from STDIN
            """);
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            usage(); return;
        }

        String key = System.getenv("BLUESKY_API_KEY");
        if (key == null) { System.err.println("BLUESKY_API_KEY missing"); System.exit(1); }

        BlueskyHttpClient.initialize("http://localhost:60610", key);
        var client = BlueskyHttpClient.get();

        try {
            if ("list".equalsIgnoreCase(args[0])) {
                Arrays.stream(ApiEndpoint.values())
                        .map(Enum::name)
                        .sorted()
                        .forEach(System.out::println);
                return;
            }

            ApiEndpoint ep;
            try {
                ep = ApiEndpoint.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException iae) {
                System.err.println("❌ Unknown endpoint. Run `list` for valid names.");
                System.exit(2);
                return;
            }

            Object body = (args.length > 1) ? parseBodyArg(args[1]) : null;
            Map<String,Object> resp = client.send(ep, body);
            System.out.println(JSON.writerWithDefaultPrettyPrinter().writeValueAsString(resp));
        }
        catch (BlueskyException be) {
            System.err.println("❌ " + be.getMessage());
            System.exit(2);
        }
        catch (Exception ex) {
            System.err.println("❌ Unexpected error: " + ex);
            System.exit(1);
        }
    }


    private static Object parseBodyArg(String arg) throws IOException {
        if (arg.startsWith("@")) {
            if (arg.equals("@-")) {
                return JSON.readValue(System.in, Object.class);
            } else {
                Path p = Path.of(arg.substring(1));
                return JSON.readValue(Files.readString(p), Object.class);
            }
        }
        return JSON.readValue(arg, Object.class);
    }
}
