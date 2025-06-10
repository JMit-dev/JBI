package com.jbi;

import com.jbi.client.BlueskyHttpClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        String baseUrl = "http://localhost:60610";
        String apiKey = System.getenv("BLUESKY_API_KEY");
        if (apiKey == null) {
            System.err.println("BLUESKY_API_KEY environment variable is not set.");
            return;
        }

        BlueskyHttpClient.initialize(baseUrl, apiKey);  // singleton init

        Parent root = FXMLLoader.load(getClass().getResource("/view/ReManagerConnection.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("Queue Monitor");
        stage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
