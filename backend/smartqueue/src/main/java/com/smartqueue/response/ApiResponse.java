package com.smartqueue.response;

import java.time.Instant;

public record ApiResponse<T> (
    String message,
    T data,
    Instant timestamp
) {
}

