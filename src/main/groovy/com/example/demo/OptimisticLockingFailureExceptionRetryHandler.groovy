package com.example.demo

import org.slf4j.Logger
import org.springframework.dao.OptimisticLockingFailureException

import java.util.concurrent.TimeUnit

class OptimisticLockingFailureExceptionRetryHandler extends RetryHandler<OptimisticLockingFailureException> {

    OptimisticLockingFailureExceptionRetryHandler(Logger logger) {
        this(logger, 5, TimeUnit.MILLISECONDS, 100)
    }

    protected OptimisticLockingFailureExceptionRetryHandler(Logger logger, int retries, TimeUnit timeUnit, long sleep) {
        super((exception, currentRetry) -> {
            try {
                if (currentRetry <= retries) {
                    try {
                        timeUnit.sleep(sleep)
                    } catch (InterruptedException ignored) {
                    }

                    if (logger != null) {
                        logger.warn("Retry {}, last error: {}", currentRetry, exception.toString())
                    }

                    return true;
                }
            } catch (Exception e) {
                if (logger == null) {
                    e.printStackTrace()
                } else {
                    logger.error(e.toString(), e)
                }
            }

            return false
        })
    }
}
