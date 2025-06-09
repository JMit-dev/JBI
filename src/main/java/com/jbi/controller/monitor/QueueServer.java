package com.jbi.controller.monitor;


import com.jbi.client.BlueskyHttpClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class QueueServer {

    @FXML
    private Button connectButton;

    @FXML
    private Label connectionStatusLabel;

    @FXML
    private Button disconnectButton;

    @FXML
    void connect(ActionEvent event) {
        setConnected(true);
    }

    @FXML
    void disconnect(ActionEvent event) {
        setConnected(false);
    }

    private void setConnected(boolean connected) {
        connectButton.setDisable(connected);
        disconnectButton.setDisable(!connected);
        connectionStatusLabel.setText(connected ? "ONLINE" : "-----");
    }

}
