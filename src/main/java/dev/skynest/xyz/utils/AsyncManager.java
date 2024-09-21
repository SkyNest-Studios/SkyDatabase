package dev.skynest.xyz.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class AsyncManager {

    private final ScheduledExecutorService service;

    public AsyncManager(int threads, String threadNamePrefix) {
        this.service = Executors.newScheduledThreadPool(
                threads,
                new NamedThreadFactory(threadNamePrefix)
        );
    }

    public CompletableFuture<Void> run(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, service);
    }

    public <U> CompletableFuture<U> run(Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, service);
    }

    public CompletableFuture<Void> runTaskLater(Runnable runnable, long delay, TimeUnit unit) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        service.schedule(() -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, delay, unit);
        return future;
    }

    public CompletableFuture<Void> runTaskLaterAsync(Runnable runnable, long delay, TimeUnit unit) {
        return CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(unit.toMillis(delay));
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, service);
    }

    public CompletableFuture<Void> runTaskTimer(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        service.scheduleAtFixedRate(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, initialDelay, period, unit);
        return future;
    }

    public CompletableFuture<Void> runTaskTimerAsync(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        return CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(unit.toMillis(initialDelay));
                while (!Thread.currentThread().isInterrupted()) {
                    runnable.run();
                    TimeUnit.MILLISECONDS.sleep(unit.toMillis(period));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, service);
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final String prefix;

        private final AtomicInteger counter = new AtomicInteger(1);

        public NamedThreadFactory(String prefix) {
            this.prefix = "skydatabase-" + prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return getThread(r);
        }


        public Thread getThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(prefix + "-" + counter.getAndIncrement());
            return thread;
        }

    }
}
