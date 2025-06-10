package com.jbi.util;

import com.jbi.api.StatusResponse;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A single instance that always holds the most-recent /api/status result
 * (or {@code null} when the server is offline).
 *
 * Widgets that need live status call
 * <pre> StatusBus.latest().addListener((obs,oldVal,newVal) -> { ... }) </pre>
 * or bind directly to the property.
 */
public final class StatusBus {

    private static final ObjectProperty<StatusResponse> LATEST = new SimpleObjectProperty<>(null);

    private StatusBus() {}

    public static ObjectProperty<StatusResponse> latest() {
        return LATEST;
    }

    public static void push(StatusResponse s) {
        if (Platform.isFxApplicationThread()) {
            LATEST.set(s);
        } else {
            Platform.runLater(() -> LATEST.set(s));
        }
    }
}
