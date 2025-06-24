package com.jbi.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

public class RePlanEditorController {

    @FXML
    private Button addBtn;

    @FXML
    private Button batchBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private TableColumn<?, ?> chkCol;

    @FXML
    private ChoiceBox<?> choiceBox;

    @FXML
    private HBox editBtn;

    @FXML
    private RadioButton instrRadBtn;

    @FXML
    private TableColumn<?, ?> paramCol;

    @FXML
    private RadioButton planRadBtn;

    @FXML
    private Button resetBtn;

    @FXML
    private Button saveBtn;

    @FXML
    private TableView<?> table;

    @FXML
    private TableColumn<?, ?> valueCol;

}
