package com.jbi.controller;

import com.jbi.client.RunEngineService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReConsoleMonitorController implements Initializable {

    @FXML private CheckBox autoscrollChk;
    @FXML private Button clearBtn;
    @FXML private TextField maxLinesField;
    @FXML private TextArea textArea;

    private static final int MIN_LINES = 10;
    private static final int MAX_LINES = 10_000;
    private volatile int maxLines = 1_000;

    private final RunEngineService svc = new RunEngineService();
    private final ExecutorService exec = Executors.newSingleThreadExecutor(
            r ->new Thread(r, "console-stream-reader"));
    private volatile boolean stopReader = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    /******** GUI CONTROLS *************************************************/
    @FXML
    void clear(ActionEvent event) {

    }

    @FXML
    void scroll(ActionEvent event) {

    }
}