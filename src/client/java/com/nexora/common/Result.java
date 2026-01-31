package com.nexora.common;

public class Result<T> {
    private Status status;
    private T value;

    public Status getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == Status.SOME || status == Status.NOTHING;
    }

    public T getValue() {
        return value;
    }

    public enum Status {
        SOME,
        NOTHING,
        FAILURE,
        TIMEOUT
    }

    public static <T> Result<T> success(T value) {
        Result<T> result = new Result<>();
        result.status = value == null ? Status.NOTHING : Status.SOME;
        result.value = value;
        return result;
    }

    public static <T> Result<T> failure() {
        Result<T> result = new Result<>();
        result.status = Status.FAILURE;
        return result;
    }

    public static <T> Result<T> timeout() {
        Result<T> result = new Result<>();
        result.status = Status.TIMEOUT;
        return result;
    }
}
