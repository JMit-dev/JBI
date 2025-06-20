package com.jbi.controller;

import com.jbi.view.ViewFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;

import java.net.URL;
import java.util.ResourceBundle;

public final class ApplicationController implements Initializable {

    @FXML private CheckBox activateDestroyChk;
    @FXML private Tab      monitorQueueTab;
    @FXML private Tab      editAndControlQueueTab;
    @FXML private MenuItem savePlanTxt, savePlanJson, savePlanYaml;

    @Override public void initialize(URL url, ResourceBundle rb) {
        monitorQueueTab.setContent(ViewFactory.MONITOR_QUEUE.get());
        editAndControlQueueTab.setContent(ViewFactory.EDIT_AND_CONTROL_QUEUE.get());
    }

    /* -------- menu skeletons – wire up later ------------------------- */
    @FXML private void toggleDestroyActivation() { /* TODO */ }
    @FXML private void savePlanTxt()             { /* TODO */ }
    @FXML private void savePlanJson()            { /* TODO */ }
    @FXML private void savePlanYaml()            { /* TODO */ }
}
