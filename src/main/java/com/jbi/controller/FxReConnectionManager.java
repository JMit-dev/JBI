package com.jbi.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class FxReConnectionManager {

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
