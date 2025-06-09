package com.jbi.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Returned by many endpoints as a simple success/error envelope. */
public record BasicResponse(
        boolean success,
        @JsonProperty("msg")      String msg,
        @JsonAlias("detail")      String detail    // some endpoints use “detail” instead of “msg”
) {}
