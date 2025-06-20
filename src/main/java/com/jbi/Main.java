package com.jbi;

import com.jbi.client.RunEngineHttpClient;
import javafx.application.Application;
import javafx.application.Platform;
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

        RunEngineHttpClient.initialize(baseUrl, apiKey);  // singleton init

        Parent root = FXMLLoader.load(getClass().getResource("/view/MonitorQueue.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("Queue Monitor");
        stage.setMinWidth(1010);
        stage.setMinHeight(710);
        stage.setOnCloseRequest((w) -> Platform.exit());
        stage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
