package com.example.demo.service

import java.util.concurrent.atomic.AtomicLong

class IdGenerator {

    public static final def INSTANCE = new IdGenerator()

    private final def id = new AtomicLong()

    private IdGenerator() {}

    long id() {
        while (true) {
            def ts = System.currentTimeMillis()

            if (ts > id.get()) {
                id.set(ts)

                break
            }

            Thread.sleep(1)
        }

        return id.get()
    }
}
