package com.example.demo

import java.util.concurrent.Callable
import java.util.function.Consumer

abstract class RetryHandler<E extends Exception> {

    private final Predicate<E> predicate

    protected RetryHandler(Predicate<E> predicate) {
        this.predicate = predicate
    }

    <T> T retry(Callable<T> callable) throws Exception {
        return retry(callable, null)
    }

    <T> T retry(Callable<T> callable, Consumer<Throwable> throwableConsumer) throws Exception {
        int currentRetry = 0
        do {
            try {
                return callable.call()
            } catch (Exception e) {
                boolean isTest
                try {
                    isTest = predicate.test((E) e, ++currentRetry)
                } catch (Exception e1) {
                    throw new InternalException(e1)
                }

                if (!isTest) {
                    throw e
                }

                if (throwableConsumer != null) {
                    try {
                        throwableConsumer.accept(e)
                    } catch (Exception e1) {
                        throw new InternalException(e1)
                    }
                }
            }
        } while (true)
    }

    @FunctionalInterface
    interface Predicate<T> {

        boolean test(T t, int currentRetry)
    }

    static class InternalException extends Exception {

        InternalException(Throwable cause) {
            super(cause)
        }
    }
}
