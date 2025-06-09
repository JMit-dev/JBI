package com.jbi.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record QueueGetPayload(
        List<QueueItem> queue,
        List<QueueItem> running,
        @JsonProperty("plan_queue_uid")   String planQueueUid,
        @JsonProperty("running_item_uid") String runningItemUid
) {}