package com.jbi.util;

import java.util.concurrent.*;
import java.util.function.Supplier;

// Single ScheduledExecutor shared by all widgets that need periodic polling.
public final class PollCenter {

    private static final ScheduledExecutorService EXEC =
            Executors.newSingleThreadScheduledExecutor(r ->
                    new Thread(r, "JBI-poller"));

    private PollCenter() {}

    public static ScheduledFuture<?> every(long periodSec, Runnable task) {
        return EXEC.scheduleAtFixedRate(task, 0, periodSec, TimeUnit.SECONDS);
    }

    public static <T> ScheduledFuture<?> every(
            long periodSec,
            Supplier<T> supplier,
            java.util.function.Consumer<T> fxConsumer) {

        return every(periodSec, () -> {
            T t = supplier.get();
            javafx.application.Platform.runLater(() -> fxConsumer.accept(t));
        });
    }
}
