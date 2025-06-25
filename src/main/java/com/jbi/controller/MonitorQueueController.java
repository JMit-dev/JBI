package com.jbi.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MonitorQueueController implements Initializable {

    @FXML private AnchorPane runningPlanContainer;
    @FXML private AnchorPane planQueueContainer;
    @FXML private AnchorPane planHistoryContainer;
    @FXML private TitledPane runningPane, queuePane, historyPane, consolePane;
    @FXML private VBox stack;

    private final Map<TitledPane, Double> savedHeights = new HashMap<>();

    private static final String BAR_NORMAL =
            "-fx-background-color: linear-gradient(to bottom, derive(-fx-base,15%) 0%, derive(-fx-base,-5%) 100%);" +
                    "-fx-border-color: derive(-fx-base,-25%) transparent derive(-fx-base,-25%) transparent;" +
                    "-fx-border-width: 1 0 1 0;";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadInto(runningPlanContainer, "/view/ReRunningPlan.fxml", new ReRunningPlanController(true));
        loadInto(planQueueContainer, "/view/RePlanQueue.fxml", new RePlanQueueController(true));
        loadInto(planHistoryContainer, "/view/RePlanHistory.fxml", new RePlanHistoryController(true));

        TitledPane[] panes = { runningPane, queuePane, historyPane, consolePane };
        for (TitledPane p : panes) {
            p.setMaxHeight(Double.MAX_VALUE);  // let VBox stretch it as high as needed
            p.setPrefHeight(0);                // start with zero so panes share space evenly
            p.expandedProperty().addListener((obs, wasExpanded, expanded) -> {
                if (expanded) {                         // RE-EXPANDING
                    // restore manual height if one was saved, else start shared
                    double h = savedHeights.getOrDefault(p, 0.0);
                    p.setPrefHeight(h);
                    VBox.setVgrow(p, Priority.ALWAYS);
                } else {                                // COLLAPSING
                    // remember current prefHeight, then shrink to header only
                    savedHeights.put(p, p.getPrefHeight());
                    p.setPrefHeight(Region.USE_COMPUTED_SIZE);
                    p.setMinHeight(Region.USE_COMPUTED_SIZE);
                    VBox.setVgrow(p, Priority.NEVER);
                }
            });

            VBox.setVgrow(p, p.isExpanded() ? Priority.ALWAYS : Priority.NEVER);
        }

        addDragBars(panes);
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

    private void addDragBars(TitledPane[] panes) {
        for (int i = 0; i < panes.length - 1; i++) {

            Pane bar = new Pane();
            bar.setStyle(BAR_NORMAL);
            bar.setPrefHeight(4);                 // thin line
            bar.setMinHeight(4);
            bar.setMaxHeight(4);
            VBox.setVgrow(bar, Priority.NEVER);

            final TitledPane upper = panes[i];
            final TitledPane lower = panes[i + 1];

            bar.setOnMouseEntered(e -> bar.setCursor(Cursor.V_RESIZE));
            bar.setOnMousePressed(e -> {
                bar.setUserData(new double[]{
                        e.getScreenY(),
                        upper.getPrefHeight(),
                        lower.getPrefHeight()
                });
            });
            bar.setOnMouseDragged(e -> {
                if (!upper.isExpanded() || !lower.isExpanded()) return;

                double[] data = (double[]) bar.getUserData();
                double dy      = e.getScreenY() - data[0];
                double newUp   = Math.max(40, data[1] + dy);   // 40 = min body height
                double newLow  = Math.max(40, data[2] - dy);

                upper.setPrefHeight(newUp);
                lower.setPrefHeight(newLow);
            });

            /* insert bar AFTER the upper pane in the VBox children list */
            int insertPos = stack.getChildren().indexOf(upper) + 1;
            stack.getChildren().add(insertPos, bar);
        }
    }
}
