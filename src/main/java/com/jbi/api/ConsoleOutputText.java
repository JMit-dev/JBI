package com.jbi.api;

public record ConsoleOutputText(
        boolean success,
        String  msg,
        String  text
) {}