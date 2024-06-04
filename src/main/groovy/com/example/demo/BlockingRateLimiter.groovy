package com.example.demo

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

class BlockingRateLimiter extends AbstractLogging {

    private static final int RING_SIZE = 60
    private static final int SAFE_OFFSET = 20
    private static final long TIMEOUT_MS = 1_000
    private static final int DELAY = (SAFE_OFFSET / 2) * 1_000

    private static final AtomicBoolean IS_RUNNING = new AtomicBoolean(true)
    private static final Collection<BlockingRateLimiter> SET = ConcurrentHashMap.newKeySet()

    private static Thread thread

    private final Semaphore[] ring = new Semaphore[RING_SIZE]
    private final int maxRatePerSecond
    private final int delay

    BlockingRateLimiter(int maxRatePerSecond) {
        this(maxRatePerSecond, 0)
    }

    BlockingRateLimiter(int maxRatePerSecond, int delay) {
        this(maxRatePerSecond, delay, false)
    }

    BlockingRateLimiter(int maxRatePerSecond, int delay, boolean fair) {
        this.maxRatePerSecond = maxRatePerSecond
        this.delay = delay * 1_000

        for (int i = 0; i < ring.length; i++) {
            ring[i] = new Semaphore(maxRatePerSecond, fair)
        }

        SET.add(this)

        init()
    }

    static void terminate() {
        IS_RUNNING.set(false)

        if (thread != null) {
            thread.interrupt()
        }
    }

    private synchronized void init() {
        if (thread == null) {
            thread = new Thread("BlockingRateLimiter-Cleaner") {
                @Override
                void run() {
                    do {
                        try {
                            SET.forEach(BlockingRateLimiter::clean)
                        } catch (Exception e) {
                            log.error(e.toString(), e)
                        }

                        try {
                            TimeUnit.MILLISECONDS.sleep(DELAY)
                        } catch (InterruptedException ignored) {
                            currentThread().interrupt()

                            break
                        }
                    } while (IS_RUNNING.get())

                    log.info("Terminate")
                }
            }

            thread.start()
        }
    }

    void acquireAccess() {
        while (!acquire()) {
            if (delay > 0) {
                LockSupport.parkNanos(delay)
//                sleep(delay)
            }
        }
    }

    private void clean() {
        int activeCell = getActiveRingCell()
        int left = (activeCell - SAFE_OFFSET + RING_SIZE) % RING_SIZE
        int right = (activeCell + SAFE_OFFSET + 1) % RING_SIZE

        for (int cell = right; cell != left; cell = nextCellInRing(cell)) {
            reset(cell)
        }
    }

    private boolean acquire() {
        try {
            return tryToAcquire(getActiveRingCell())
        } catch (InterruptedException ignored) {
            return false
        }
    }

    private boolean tryToAcquire(int cell) throws InterruptedException {
        Semaphore semaphore = ring[cell]

        return semaphore.tryAcquire(TIMEOUT_MS, TimeUnit.MILLISECONDS)
    }

    private static int getActiveRingCell() {
        return (int) ((System.currentTimeMillis() / 1_000) % RING_SIZE)
    }

    private static int nextCellInRing(int cell) {
        return (cell + 1) % RING_SIZE
    }

    private void reset(int cell) {
        Semaphore semaphore = ring[cell]
        semaphore.drainPermits()
        semaphore.release(maxRatePerSecond)
    }
}