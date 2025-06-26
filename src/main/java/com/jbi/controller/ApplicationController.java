package com.jbi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jbi.api.HistoryGetPayload;
import com.jbi.api.QueueItem;
import com.jbi.client.RunEngineService;
import com.jbi.util.UiSignals;
import com.jbi.view.ViewFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public final class ApplicationController implements Initializable {

    @FXML private CheckBox activateDestroyChk;
    @FXML private Tab      monitorQueueTab;
    @FXML private Tab      editAndControlQueueTab;
    @FXML private MenuItem savePlanTxt   , savePlanJson, savePlanYaml, activateDestroyItem;

    private final RunEngineService svc = new RunEngineService();
    private static final Logger LOG = Logger.getLogger(ApplicationController.class.getName());

    @Override public void initialize(URL url, ResourceBundle rb) {
        monitorQueueTab.setContent(ViewFactory.MONITOR_QUEUE.get());
        editAndControlQueueTab.setContent(ViewFactory.EDIT_AND_CONTROL_QUEUE.get());

        activateDestroyChk.selectedProperty()
                .bindBidirectional(UiSignals.envDestroyArmedProperty());
    }

    @FXML private void toggleDestroyActivation() {
        activateDestroyChk.setSelected(!activateDestroyChk.isSelected());
    }

    @FXML private void savePlanTxt() {
        saveHistory(PlanHistorySaver.Format.TXT, "txt");
    }

    @FXML private void savePlanJson() {
        saveHistory(PlanHistorySaver.Format.JSON, "json");
    }

    @FXML private void savePlanYaml() {
        saveHistory(PlanHistorySaver.Format.YAML, "yaml");
    }

    private void saveHistory(PlanHistorySaver.Format fmt, String ext) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(ext.toUpperCase(), "."+ext));
        fc.setInitialFileName("plan_history." + ext);
        File f = fc.showSaveDialog(monitorQueueTab.getContent().getScene().getWindow());
        if (f == null) return;

        Platform.runLater(() -> {
            try {
                HistoryGetPayload hp = svc.historyGetTyped();
                List<QueueItem> items = hp.items();
                PlanHistorySaver.save(items, f, fmt);
                LOG.info(() -> "Saved plan history → " + f);
            } catch (Exception e) {
                LOG.warning("Save history failed: " + e.getMessage());
            }
        });
    }

    private static final class PlanHistorySaver {

        private static final ObjectMapper JSON =
                (ObjectMapper) new ObjectMapper()
                        .writerWithDefaultPrettyPrinter()
                        .withDefaultPrettyPrinter()
                        .getFactory()
                        .getCodec();

        private static final DateTimeFormatter TS =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                        .withZone(ZoneId.systemDefault());

        private PlanHistorySaver() {}
        enum Format {TXT, JSON, YAML}

        static void save(List<QueueItem> history, File file, Format fmt) throws Exception {
            switch (fmt) {
                case TXT  -> writeTxt(history, file);
                case JSON -> JSON.writerWithDefaultPrettyPrinter().writeValue(file, history);
                case YAML -> new ObjectMapper(new YAMLFactory())
                        .writerWithDefaultPrettyPrinter()
                        .writeValue(file, history);
            }
        }

        private static void writeTxt(List<QueueItem> h, File f) throws Exception {
            try (BufferedWriter w = Files.newBufferedWriter(f.toPath())) {

                int idx = 0;
                for (QueueItem qi : h) {

                    Map<String, Object> res = qi.result();
                    double t0 = res == null ? 0 : ((Number)res.getOrDefault("time_start",0)).doubleValue();
                    double t1 = res == null ? 0 : ((Number)res.getOrDefault("time_stop" ,0)).doubleValue();

                    w.write("=".repeat(80)); w.newLine();

                    String hdr = "PLAN " + (++idx);
                    if (t0>0) {
                        hdr += ": " + TS.format(Instant.ofEpochMilli((long)(t0*1_000)));
                        if (t1>0)
                            hdr += " - " + TS.format(Instant.ofEpochMilli((long)(t1*1_000)));
                    }
                    w.write(center(hdr,80)); w.newLine();
                    w.write("=".repeat(80)); w.newLine();

                    String pretty = JSON.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(JSON.convertValue(qi, Map.class));
                    w.write(pretty); w.newLine(); w.newLine();
                }
            }
        }

        private static String center(String text, int width) {
            int pad = Math.max(0, (width - text.length()) / 2);
            return " ".repeat(pad) + text;
        }
    }
}
