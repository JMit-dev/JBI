package com.jbi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbi.api.ConsoleOutputUpdate;
import com.jbi.client.RunEngineService;
import com.jbi.util.PollCenter;
import com.jbi.util.StatusBus;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

public final class ReConsoleMonitorController implements Initializable {

    @FXML private CheckBox autoscrollChk;
    @FXML private Button clearBtn;
    @FXML private TextField maxLinesField;
    @FXML private TextArea textArea;

    private static final int MIN_LINES = 10, MAX_LINES = 10_000, BACKLOG = 1_000, CHUNK = 32_768;

    private int maxLines = 1_000;

    private final RunEngineService svc = new RunEngineService();
    private final ExecutorService io = Executors.newSingleThreadExecutor(r -> new Thread(r, "console-reader"));
    private static final ObjectMapper JSON = new ObjectMapper();
    private final Deque<String> buf = new ArrayDeque<>(MAX_LINES);

    private volatile boolean stop = true;
    private volatile String lastUid = "ALL";
    private Instant lastLine = Instant.EPOCH;

    private ScheduledFuture<?> pollTask;
    private boolean ignoreScrollbar = false;
    private boolean userScrolled = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: monospace");

        textArea.sceneProperty().addListener((o, ov, nv) -> {
            if (nv == null) return;
            Platform.runLater(() -> {
                ScrollBar v = (ScrollBar) textArea.lookup(".scroll-bar:vertical");
                if (v != null) v.valueProperty().addListener(this::scrollbarChanged);
            });
        });

        StringConverter<Integer> cv = new IntegerStringConverter();
        TextFormatter<Integer> tf = new TextFormatter<>(cv, maxLines, c -> c.getControlNewText().matches("\\d*") ? c : null);
        maxLinesField.setTextFormatter(tf);

        tf.valueProperty().addListener((o, ov, nv) -> {
            maxLines = clamp(nv == null ? MIN_LINES : nv);
            maxLinesField.setText(String.valueOf(maxLines));
            render();
        });

        maxLinesField.focusedProperty().addListener((o, ov, nv) -> {
            if (!nv) maxLinesField.setText(String.valueOf(maxLines));
        });

        clearBtn.setOnAction(e -> {
            buf.clear();
            textArea.clear();
        });

        StatusBus.latest().addListener((o, oldS, newS) ->
                Platform.runLater(() -> {
                    if (newS != null) start();
                    else stop();
                }));
    }

    public void shutdown() {
        stop();
        io.shutdownNow();
    }

    private void start() {
        if (pollTask != null) return;
        stop = false;
        buf.clear();
        textArea.clear();
        lastUid = "ALL";
        loadBacklog();
        startStream();
        pollTask = PollCenter.every(1, this::poll);
    }

    private void stop() {
        if (pollTask != null) {
            pollTask.cancel(true);
            pollTask = null;
        }
        stop = true;
    }

    private void loadBacklog() {
        try {
            String t = svc.consoleOutput(BACKLOG).text();
            if (!t.isEmpty()) pushChunk(t);
            lastUid = svc.consoleOutputUid().uid();
            render();
        } catch (Exception ignore) {}
    }

    private void startStream() {
        Task<Void> job = new Task<>() {
            @Override
            protected Void call() {
                try (var br = new BufferedReader(
                        new InputStreamReader(svc.streamConsoleOutput(), StandardCharsets.UTF_8))) {
                    StringBuilder chunk = new StringBuilder(CHUNK);
                    for (String ln; !stop && (ln = br.readLine()) != null; ) {
                        if (!ln.isBlank())
                            chunk.append(JSON.readTree(ln).path("msg").asText()).append('\n');
                        if (chunk.length() > CHUNK) {
                            String out = chunk.toString();
                            chunk.setLength(0);
                            Platform.runLater(() -> {
                                pushChunk(out);
                                render();
                            });
                        }
                    }
                    if (chunk.length() > 0)
                        Platform.runLater(() -> {
                            pushChunk(chunk.toString());
                            render();
                        });
                } catch (Exception ignore) {}
                return null;
            }
        };
        io.submit(job);
    }

    private void poll() {
        if (stop || Instant.now().minusMillis(1000).isBefore(lastLine)) return;
        try {
            ConsoleOutputUpdate u = svc.consoleOutputUpdate(lastUid);
            if (u.consoleOutputMsgs() != null) {
                StringBuilder sb = new StringBuilder();
                for (Map<String, Object> m : u.consoleOutputMsgs())
                    sb.append((String) m.get("msg"));
                if (sb.length() != 0) {
                    pushChunk(sb.toString());
                    render();
                }
            }
            lastUid = u.lastMsgUid();
        } catch (Exception ignore) {}
    }

    private void pushChunk(String c) {
        int s = 0, i;
        while ((i = c.indexOf('\n', s)) >= 0) {
            addLine(c.substring(s, i + 1));
            s = i + 1;
        }
        if (s < c.length()) addLine(c.substring(s));
    }

    private void addLine(String l) {
        if (l.isEmpty()) return;
        buf.addLast(l);
        if (buf.size() > MAX_LINES) buf.removeFirst();
    }

    private void render() {
        boolean wantBottom = autoscrollChk.isSelected();

        ignoreScrollbar = true;
        int caretPosition = textArea.getCaretPosition();
        double scrollTop = textArea.getScrollTop();

        StringBuilder sb = new StringBuilder();
        int skip = Math.max(0, buf.size() - maxLines), k = 0;
        for (String l : buf) if (k++ >= skip) sb.append(l);

        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }

        textArea.setText(sb.toString());

        if (wantBottom) {
            textArea.positionCaret(textArea.getLength());
        } else {
            textArea.positionCaret(Math.min(caretPosition, textArea.getLength()));
            Platform.runLater(() -> textArea.setScrollTop(scrollTop));
        }

        ignoreScrollbar = false;
        lastLine = Instant.now();
    }

    private void scrollbarChanged(ObservableValue<? extends Number> o, Number ov, Number nv) {
        if (ignoreScrollbar) return;
        ScrollBar v = (ScrollBar) textArea.lookup(".scroll-bar:vertical");
        if (v != null) {
            boolean atBottom = nv.doubleValue() >= v.getMax();
            userScrolled = !atBottom;
        }
    }

    private static int clamp(int v) {
        return Math.max(MIN_LINES, Math.min(MAX_LINES, v));
    }
}
