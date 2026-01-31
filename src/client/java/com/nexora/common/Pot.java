package com.nexora.common;

import java.util.concurrent.TimeUnit;
import com.nexora.Mcqq;

public class Pot<T> {
    private volatile Result<T> result = null;

    public Pot() {}

    public Result<T> get(long timeout, TimeUnit timeUnit) {
        synchronized (this) {
            if (result == null) {
                try {
                    wait(timeUnit.toMillis(timeout));
                } catch (InterruptedException e) {
                    Mcqq.LOGGER.warn("Interrupted when waiting for result {}", e);
                    return Result.failure();
                }

                if (result == null) {
                    return Result.timeout();
                }
            }
            return result;
        }
    }

    public void set(T value) {
        synchronized (this) {
            if (result == null) {
                result = Result.success(value);
                notifyAll();
            }
        }
    }

    public void setFail() {
        synchronized (this) {
            if (result == null) {
                result = Result.failure();
                notifyAll();
            }
        }
    }
}
