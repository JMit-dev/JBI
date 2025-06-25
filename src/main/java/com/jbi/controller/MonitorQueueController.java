package com.jbi.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MonitorQueueController implements Initializable {

    @FXML private AnchorPane runningPlanContainer;
    @FXML private AnchorPane planQueueContainer;
    @FXML private AnchorPane planHistoryContainer;
    @FXML private TitledPane runningPane, queuePane, historyPane, consolePane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadInto(runningPlanContainer, "/view/ReRunningPlan.fxml", new ReRunningPlanController(true));
        loadInto(planQueueContainer, "/view/RePlanQueue.fxml", new RePlanQueueController(true));
        loadInto(planHistoryContainer, "/view/RePlanHistory.fxml", new RePlanHistoryController(true));

        TitledPane[] panes = { runningPane, queuePane, historyPane, consolePane };
        for (TitledPane p : panes) {
            p.setMaxHeight(Double.MAX_VALUE);  // let VBox stretch it as high as needed
            p.setPrefHeight(0);                // start with zero so panes share space evenly
            p.expandedProperty().addListener(
                    (obs, oldVal, expanded) ->
                            VBox.setVgrow(p, expanded ? Priority.ALWAYS : Priority.NEVER)
            );
            VBox.setVgrow(p, p.isExpanded() ? Priority.ALWAYS : Priority.NEVER);
        }
    }

    private void loadInto(AnchorPane container, String fxml, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setController(controller);
            Parent view = loader.load();

            container.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
        } catch (IOException e) {
            e.printStackTrace(); // or log
        }
    }

}
