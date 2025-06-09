package com.jbi;

import com.jbi.client.BlueskyHttpClient;
import com.jbi.api.StatusResponse;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load FXML from resources/view/Hello.fxml
        Parent root = FXMLLoader.load(getClass().getResource("/view/monitor/QueueServer.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("JavaFX FXML Example");
        stage.show();
        new Thread(this::startBlueskyHttpClient).start();
    }


    public static void main(String[] args) {
        launch(args);

    }

    private void startBlueskyHttpClient() {
            String baseUrl = "http://localhost:60610";
            String apiKey = System.getenv("BLUESKY_API_KEY");
            if (apiKey == null) {
                System.err.println("BLUESKY_API_KEY environment variable is not set.");
                return;
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
