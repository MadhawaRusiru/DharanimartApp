package lk.dharanimart.mobile;

import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PriorityExecutor extends ThreadPoolExecutor {

    public PriorityExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<Runnable>());
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new PriorityFutureTask<>((PriorityRunnable) runnable, value);
    }

    public static class PriorityFutureTask<T> extends FutureTask<T> implements Comparable<PriorityFutureTask<T>> {

        private final PriorityRunnable priorityRunnable;

        public PriorityFutureTask(PriorityRunnable priorityRunnable, T result) {
            super(priorityRunnable, result);
            this.priorityRunnable = priorityRunnable;
        }

        @Override
        public int compareTo(PriorityFutureTask<T> another) {
            return Integer.compare(priorityRunnable.getPriority(), another.priorityRunnable.getPriority());
        }
    }

    public interface PriorityRunnable extends Runnable {
        int getPriority();
    }
}
