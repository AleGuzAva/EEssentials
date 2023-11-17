package EEssentials.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

public class AsynchronousUtil {
    private static final ExecutorService ASYNC_EXEC = Executors.newFixedThreadPool(8,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("EEssentials Thread")
                    .build());

    /**
     * Runs a task off thread.
     * @param callable The task to be run.
     * @param <T> The return type of the task.
     * @return A {@link CompletableFuture<T>} of the task.
     */
    public static <T> CompletableFuture<T> runTaskAsynchronously(Callable<T> callable) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
                throw new CompletionException(e);
            }
        }, ASYNC_EXEC);
    }

    public static void shutdown() {
        ASYNC_EXEC.shutdownNow();
    }
}
