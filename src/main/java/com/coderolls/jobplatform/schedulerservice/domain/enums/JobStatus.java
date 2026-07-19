package com.coderolls.jobplatform.schedulerservice.domain.enums;

public enum JobStatus {
    CREATED,
    DISPATCHING,
    READY,              // Processor accepted the request, queued, not yet executing
    PROCESSING,         // renamed from RUNNING
    SKIPPED_CONCURRENT,
    PROCESSOR_UNAVAILABLE,
    RETRY_SCHEDULED,
    SUCCESS,
    FAILED,
    TIMED_OUT,
    MAX_RETRIES_EXCEEDED
}