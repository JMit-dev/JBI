package com.jbi.controller;

import com.jbi.api.StatusResponse;
import com.jbi.client.BlueskyService;
import com.jbi.util.PollCenter;
import com.jbi.util.StatusBus;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.concurrent.ScheduledFuture;

public final class ReManagerConnectionController {

    @FXML private Button connectButton;
    @FXML private Button disconnectButton;
    @FXML private Label  connectionStatusLabel;

    private final BlueskyService svc = new BlueskyService();
    private ScheduledFuture<?>   pollTask;
    private static final int PERIOD_SEC = 1;

    @FXML private void connect()    { startPolling(); }
    @FXML private void disconnect() { stopPolling();  }

    private void startPolling() {
        if (pollTask != null && !pollTask.isDone()) return;     // already running
        showPending();                                          // UI while waiting

        updateWidgets(queryStatusOnce());

        pollTask = PollCenter.every(PERIOD_SEC,
                this::queryStatusOnce,      // background
                this::updateWidgets);       // FX thread
    }

    private StatusResponse queryStatusOnce() {
        try {
            return svc.status();
        } catch (Exception ex) {
            return null;
        }
    }

    private void stopPolling() {
        if (pollTask != null) pollTask.cancel(true);
        pollTask = null;
        StatusBus.push(null);
        showIdle();
    }

    private void showPending() {
        connectionStatusLabel.setText("-----");
        connectButton.setDisable(true);
        disconnectButton.setDisable(false);
    }
    private void showIdle() {
        connectionStatusLabel.setText("-----");
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
    }
    private void showOnline() {
        connectionStatusLabel.setText("ONLINE");
        connectButton.setDisable(true);
        disconnectButton.setDisable(false);
    }

    private void updateWidgets(StatusResponse s) {
        StatusBus.push((s));
        if (s != null) showOnline();
        else        showPending();      // keep polling; user may Disconnect
    }
}
