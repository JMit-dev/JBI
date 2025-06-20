package com.jbi;

import com.jbi.client.RunEngineHttpClient;
import com.jbi.view.ViewFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override public void start(Stage stage) throws Exception {

        String baseUrl = "http://localhost:60610";
        String apiKey  = System.getenv("BLUESKY_API_KEY");
        if (apiKey == null) {
            System.err.println("BLUESKY_API_KEY environment variable is not set.");
            Platform.exit();
            return;
        }

        RunEngineHttpClient.initialize(baseUrl, apiKey);

        Parent root = ViewFactory.APPLICATION.get();
        stage.setScene(new Scene(root));
        stage.setTitle("Queue Monitor");
        stage.setMinWidth(1010);
        stage.setMinHeight(710);
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
