package com.jbi.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record ConsoleOutputUpdate(
        boolean            success,
        String             msg,
        List<Map<String,Object>>  msgs,
        @JsonProperty("last_msg_uid")
        String             lastMsgUid
) {}