package com.basefinder.engine;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

/** Runs a batch of tasks on a pool with a bounded concurrency window, then awaits all. */
final class ParallelRunner {

    private ParallelRunner() {
    }

    static void runAll(ExecutorService pool, List<Runnable> tasks, int maxConcurrency) throws InterruptedException {
        if (tasks.isEmpty()) {
            return;
        }
        Semaphore permits = new Semaphore(Math.max(1, maxConcurrency));
        CountDownLatch latch = new CountDownLatch(tasks.size());
        for (Runnable task : tasks) {
            permits.acquire();
            pool.execute(() -> {
                try {
                    task.run();
                } finally {
                    permits.release();
                    latch.countDown();
                }
            });
        }
        latch.await();
    }
}
