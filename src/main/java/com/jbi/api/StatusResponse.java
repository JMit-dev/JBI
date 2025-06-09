package com.jbi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Exact shape of `/api/status` JSON. */
public record StatusResponse(
        String msg,
        @JsonProperty("manager_state")           String  managerState,
        @JsonProperty("worker_environment_exists") boolean workerEnvironmentExists,
        @JsonProperty("worker_environment_state") String  workerEnvironmentState,
        @JsonProperty("re_state")                String  reState,
        int     itemsInQueue,
        int     itemsInHistory,
        String  runningItemUid,
        String  planQueueUid,
        String  planHistoryUid,
        String  devicesExistingUid,
        String  plansExistingUid,
        String  devicesAllowedUid,
        String  plansAllowedUid,
        String  taskResultsUid,
        String  lockInfoUid,
        boolean queueStopPending,
        boolean queueAutostartEnabled,
        int     workerBackgroundTasks,
        String  ipKernelState,
        Boolean ipKernelCaptured,
        boolean pausePending,
        String  runListUid,
        PlanQueueMode planQueueMode,
        LockInfo lock
) {
    /** Nested object returned under `"lock"` */
    public record LockInfo(boolean environment, boolean queue) {}

    /** Nested object returned under `"plan_queue_mode"` */
    public record PlanQueueMode(boolean loop, boolean ignoreFailures) {}
}
