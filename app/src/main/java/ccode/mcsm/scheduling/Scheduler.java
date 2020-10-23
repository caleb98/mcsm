package ccode.mcsm.scheduling;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Scheduler {
	
	//TODO: way to set scheduler pool size (will we really ever need more than 2 threads, though?)
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

	/**
	 * @param command
	 * @param delay
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ScheduledExecutorService#schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit)
	 */
	public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return scheduler.schedule(command, delay, unit);
	}

	/**
	 * @param command
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	public static void execute(Runnable command) {
		scheduler.execute(command);
	}

	/**
	 * @param <V>
	 * @param callable
	 * @param delay
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ScheduledExecutorService#schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit)
	 */
	public static <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return scheduler.schedule(callable, delay, unit);
	}

	/**
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 */
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	/**
	 * 
	 * @see java.util.concurrent.ExecutorService#shutdown()
	 */
	public static void shutdown() {
		scheduler.shutdown();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ExecutorService#shutdownNow()
	 */
	public static List<Runnable> shutdownNow() {
		return scheduler.shutdownNow();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ExecutorService#isShutdown()
	 */
	public static boolean isShutdown() {
		return scheduler.isShutdown();
	}

	/**
	 * @param command
	 * @param initialDelay
	 * @param delay
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(java.lang.Runnable, long, long, java.util.concurrent.TimeUnit)
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	/**
	 * @return
	 * @see java.util.concurrent.ExecutorService#isTerminated()
	 */
	public static boolean isTerminated() {
		return scheduler.isTerminated();
	}

	/**
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @see java.util.concurrent.ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
	 */
	public static boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return scheduler.awaitTermination(timeout, unit);
	}

	/**
	 * @param <T>
	 * @param task
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)
	 */
	public static <T> Future<T> submit(Callable<T> task) {
		return scheduler.submit(task);
	}

	/**
	 * @param <T>
	 * @param task
	 * @param result
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable, java.lang.Object)
	 */
	public static <T> Future<T> submit(Runnable task, T result) {
		return scheduler.submit(task, result);
	}

	/**
	 * @param task
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable)
	 */
	public static Future<?> submit(Runnable task) {
		return scheduler.submit(task);
	}

	/**
	 * @param <T>
	 * @param tasks
	 * @return
	 * @throws InterruptedException
	 * @see java.util.concurrent.ExecutorService#invokeAll(java.util.Collection)
	 */
	public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return scheduler.invokeAll(tasks);
	}

	/**
	 * @param <T>
	 * @param tasks
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @see java.util.concurrent.ExecutorService#invokeAll(java.util.Collection, long, java.util.concurrent.TimeUnit)
	 */
	public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return scheduler.invokeAll(tasks, timeout, unit);
	}

	/**
	 * @param <T>
	 * @param tasks
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @see java.util.concurrent.ExecutorService#invokeAny(java.util.Collection)
	 */
	public static <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return scheduler.invokeAny(tasks);
	}

	/**
	 * @param <T>
	 * @param tasks
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 * @see java.util.concurrent.ExecutorService#invokeAny(java.util.Collection, long, java.util.concurrent.TimeUnit)
	 */
	public static <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return scheduler.invokeAny(tasks, timeout, unit);
	}
	
	

}
