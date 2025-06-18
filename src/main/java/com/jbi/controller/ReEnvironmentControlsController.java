package com.jbi.controller;

import com.jbi.api.StatusResponse;
import com.jbi.client.RunEngineService;
import com.jbi.util.StatusBus;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public final class ReEnvironmentControlsController implements Initializable {

    @FXML private Button openBtn;
    @FXML private Button closeBtn;
    @FXML private Button destroyBtn;

    private final RunEngineService svc = new RunEngineService();
    private final Logger LOG = Logger.getLogger(getClass().getName());

    private volatile boolean destroyArmed = false;

    @Override public void initialize(URL url, ResourceBundle rb) {

        /* react to /api/status pushes */
        StatusBus.latest().addListener(this::onStatus);

        /* Ctrl-click arms “Destroy” */
        destroyBtn.setOnMousePressed(e -> {
            if (e.isControlDown()) destroyArmed = true;
            refreshButtons(StatusBus.latest().get());
        });
        destroyBtn.focusedProperty().addListener(
                (ObservableValue<? extends Boolean> o, Boolean oldV, Boolean newV) -> {
                    if (!newV) destroyArmed = false;       // disarm if focus lost
                });
    }

    private void onStatus(ObservableValue<?> src, Object oldV, Object newV) {
        Platform.runLater(() -> refreshButtons(newV));
    }

    @SuppressWarnings("unchecked")
    private void refreshButtons(Object statusObj) {

        boolean connected     = statusObj != null;
        boolean workerExists  = false;
        String  managerState  = null;

        if (statusObj instanceof StatusResponse s) {          // preferred
            workerExists = s.workerEnvironmentExists();
            managerState = s.managerState();
        } else if (statusObj instanceof Map<?,?> m) {         // fall-back
            workerExists = Boolean.TRUE.equals(m.get("worker_environment_exists"));
            managerState = String.valueOf(m.get("manager_state"));
        }

        boolean idle = "idle".equals(managerState);

        openBtn   .setDisable(!(connected && !workerExists && idle));
        closeBtn  .setDisable(!(connected &&  workerExists && idle));
        destroyBtn.setDisable(!(connected &&  workerExists && destroyArmed));
    }

    @FXML private void open() {
        try { svc.environmentOpen(); }
        catch (Exception ex) { LOG.warning("environmentOpen: " + ex); }
    }

    @FXML private void close() {
        try { svc.environmentClose(); }
        catch (Exception ex) { LOG.warning("environmentClose: " + ex); }
    }

    @FXML private void destroy() {
        if (!destroyArmed) return;          // should be disabled, extra guard
        try { svc.environmentDestroy(); }
        catch (Exception ex) { LOG.warning("environmentDestroy: " + ex); }
        finally {
            destroyArmed = false;           // always disarm and refresh UI
            refreshButtons(StatusBus.latest().get());
        }
    }
}
