package com.coderolls.jobplatform.schedulerservice.domain.enums;

public enum JobStatus {
    CREATED,
    DISPATCHING,
    RECEIVED,           // Processor accepted the request, i.e. received at processor side
    PROCESSING,         // renamed from RUNNING
    SKIPPED_CONCURRENT,
    PROCESSOR_UNAVAILABLE,
    RETRY_SCHEDULED,
    SUCCESS,
    FAILED,
    TIMED_OUT,
    MAX_RETRIES_EXCEEDED;

    public boolean isTerminal() {
        return this == SUCCESS
                || this == FAILED
                || this == TIMED_OUT
                || this == MAX_RETRIES_EXCEEDED;
    }
}