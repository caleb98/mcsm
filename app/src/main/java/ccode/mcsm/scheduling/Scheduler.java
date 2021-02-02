package ccode.mcsm.scheduling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.action.Action;

public class Scheduler {
	
	private static final ScheduledThreadPoolExecutor scheduler;
	private static final HashMap<String, Schedule> scheduledTasks = new HashMap<>();
	
	private static final Pattern HOURLY_TASK_TIME = Pattern.compile("\t(\\d{2}:\\d{2}) (\\w+)(.*)");
	private static final long HOUR_MILLIS = 60 * 60 * 1000;
	private static final Pattern DAILY_TASK_TIME = Pattern.compile("\t(\\d{2}:\\d{2}:\\d{2}) (\\w+)(.*)");
	private static final long DAY_MILLIS = HOUR_MILLIS * 24;
	
	static {
		//TODO: way to set scheduler pool size (will we really ever need more than 2 threads, though?)
		scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(2);
		scheduler.setRemoveOnCancelPolicy(true);
	}
	
	public static synchronized void loadSchedules(MinecraftServerManager manager, File schedulesFile) {
		if(!schedulesFile.exists()) {
			try {
				schedulesFile.createNewFile();
			} catch (IOException e) {
				System.err.printf("Error creating schedules file: %s\n", e.getMessage());
				return;
			}
		}
		
		try(
				BufferedReader schedulesReader = new BufferedReader(new FileReader(schedulesFile));
		) {
			
			int lineNum = 0;
			String line;
			Matcher m;
		
			String currentScope = null;
			int hourlyNum = 0;
			int dailyNum = 0;
			
			LocalTime now;
			
			while((line = schedulesReader.readLine()) != null) {
				lineNum++;
				
				if(line.matches("Hourly") || line.matches("Daily")) {
					currentScope = line;
					continue;
				}
				
				if(line.matches("\\s*")) {
					continue;
				}
				
				switch(currentScope) {
					
				case "Hourly":
					m = HOURLY_TASK_TIME.matcher(line);
					if(m.matches()) {
						String executeTime = m.group(1);
						String actionID = m.group(2);
						String args = m.group(3).trim();
						
						if(Action.get(actionID) == null) {
							System.err.printf("Error reading schedules file on line %d: specified action %s does not exist.\n", 
									lineNum, actionID);
							continue;
						}
						
						now = LocalTime.now();
						
						String fullTime = String.format("%s:%s", now.getHour(), executeTime);
						LocalTime executeAt = LocalTime.parse(fullTime);
						
						if(executeAt.isBefore(now)) {
							executeAt = executeAt.plusHours(1);
						}
						
						long delayMillis = ChronoUnit.MILLIS.between(now, executeAt);
						
						String scheduleID = "Hourly-" + (hourlyNum++);
						ScheduledFuture<?> future = scheduleAtFixedRate(
								()->{
									Action.get(actionID).execute(manager, MinecraftServerManager.MCSM_EXECUTOR, args);
								}, 
								delayMillis, 
								HOUR_MILLIS, 
								TimeUnit.of(ChronoUnit.MILLIS)
						);
						Schedule schedule = new Schedule(scheduleID, future, MinecraftServerManager.MCSM_EXECUTOR, 
								null, actionID, args);
						registerSchedule(scheduleID, schedule);
					}
					else {
						System.err.printf("Unable to parse schedule from schedules file on line %d: %s\n", lineNum, line);
						continue;
					}
					break;
					
				case "Daily":
					m = DAILY_TASK_TIME.matcher(line);
					if(m.matches()) {
						String executeTime = m.group(1);
						String actionID = m.group(2);
						String args = m.group(3).trim();
						
						if(Action.get(actionID) == null) {
							System.err.printf("Error reading schedules file on line %d: specified action %s does not exist.\n", 
									lineNum, actionID);
							continue;
						}
						
						now = LocalTime.now();
						LocalTime executeAt = LocalTime.parse(executeTime);
						long delayMillis = (ChronoUnit.MILLIS.between(now, executeAt) + DAY_MILLIS) % DAY_MILLIS;
						
						String scheduleID = "Daily-" + (dailyNum++);
						ScheduledFuture<?> future = scheduleAtFixedRate(
								()->{
									Action.get(actionID).execute(manager, MinecraftServerManager.MCSM_EXECUTOR, args);
								},
								delayMillis,
								DAY_MILLIS,
								TimeUnit.of(ChronoUnit.MILLIS)
						);
						Schedule schedule = new Schedule(scheduleID, future, MinecraftServerManager.MCSM_EXECUTOR,
								null, actionID, args);
						registerSchedule(scheduleID, schedule);
					}
					else {
						System.err.printf("Unable to parse schedule from schedules file on line %d: %s\n", lineNum, line);
						continue;
					}
					break;
					
				default:
					break;
				
				}
			}
		} catch (IOException e) {
			System.err.printf("Error reading schedules file: %s\nNo schedules loaded.", e.getMessage());
		}
	}
	
	public static synchronized void registerSchedule(String scheduleName, Schedule schedule) {
		cleanupSchedules();
		scheduledTasks.put(scheduleName, schedule);
	}
	
	public static synchronized void cancelSchedule(String scheduleName) {
		cleanupSchedules();
		Schedule schedule = scheduledTasks.get(scheduleName);
		if(schedule != null) {
			//Use false here since we can't cancel tasks that are already running
			schedule.future.cancel(false);
			scheduledTasks.remove(scheduleName);
		}
	}
	
	public static synchronized Schedule getSchedule(String scheduleID) {
		cleanupSchedules();
		return scheduledTasks.get(scheduleID);
	}
	
	public static synchronized Set<String> getSchedules() {
		cleanupSchedules();
		return scheduledTasks.keySet();
	}
	
	public static synchronized void cleanupSchedules() {
		Iterator<Map.Entry<String, Schedule>> iter = scheduledTasks.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, Schedule> entry = iter.next();
			if(entry.getValue().future.isDone() || entry.getValue().future.isCancelled()) {
				iter.remove();
			}
		}
	}

	/**
	 * @param value
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean)
	 */
	public static void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
		scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(value);
	}

	/**
	 * @return
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#getContinueExistingPeriodicTasksAfterShutdownPolicy()
	 */
	public static boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
		return scheduler.getContinueExistingPeriodicTasksAfterShutdownPolicy();
	}

	/**
	 * @param value
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean)
	 */
	public static void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
		scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(value);
	}

	/**
	 * @return
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#getExecuteExistingDelayedTasksAfterShutdownPolicy()
	 */
	public static boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
		return scheduler.getExecuteExistingDelayedTasksAfterShutdownPolicy();
	}

	/**
	 * @param value
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#setRemoveOnCancelPolicy(boolean)
	 */
	public static void setRemoveOnCancelPolicy(boolean value) {
		scheduler.setRemoveOnCancelPolicy(value);
	}

	/**
	 * @return
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#getRemoveOnCancelPolicy()
	 */
	public static boolean getRemoveOnCancelPolicy() {
		return scheduler.getRemoveOnCancelPolicy();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ScheduledThreadPoolExecutor#getQueue()
	 */
	public static BlockingQueue<Runnable> getQueue() {
		return scheduler.getQueue();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#isTerminating()
	 */
	public static boolean isTerminating() {
		return scheduler.isTerminating();
	}

	/**
	 * @param threadFactory
	 * @see java.util.concurrent.ThreadPoolExecutor#setThreadFactory(java.util.concurrent.ThreadFactory)
	 */
	public static void setThreadFactory(ThreadFactory threadFactory) {
		scheduler.setThreadFactory(threadFactory);
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getThreadFactory()
	 */
	public static ThreadFactory getThreadFactory() {
		return scheduler.getThreadFactory();
	}

	/**
	 * @param handler
	 * @see java.util.concurrent.ThreadPoolExecutor#setRejectedExecutionHandler(java.util.concurrent.RejectedExecutionHandler)
	 */
	public static void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
		scheduler.setRejectedExecutionHandler(handler);
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getRejectedExecutionHandler()
	 */
	public static RejectedExecutionHandler getRejectedExecutionHandler() {
		return scheduler.getRejectedExecutionHandler();
	}

	/**
	 * @param corePoolSize
	 * @see java.util.concurrent.ThreadPoolExecutor#setCorePoolSize(int)
	 */
	public static void setCorePoolSize(int corePoolSize) {
		scheduler.setCorePoolSize(corePoolSize);
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getCorePoolSize()
	 */
	public static int getCorePoolSize() {
		return scheduler.getCorePoolSize();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#prestartCoreThread()
	 */
	public static boolean prestartCoreThread() {
		return scheduler.prestartCoreThread();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#prestartAllCoreThreads()
	 */
	public static int prestartAllCoreThreads() {
		return scheduler.prestartAllCoreThreads();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#allowsCoreThreadTimeOut()
	 */
	public static boolean allowsCoreThreadTimeOut() {
		return scheduler.allowsCoreThreadTimeOut();
	}

	/**
	 * @param value
	 * @see java.util.concurrent.ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)
	 */
	public static void allowCoreThreadTimeOut(boolean value) {
		scheduler.allowCoreThreadTimeOut(value);
	}

	/**
	 * @param maximumPoolSize
	 * @see java.util.concurrent.ThreadPoolExecutor#setMaximumPoolSize(int)
	 */
	public static void setMaximumPoolSize(int maximumPoolSize) {
		scheduler.setMaximumPoolSize(maximumPoolSize);
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getMaximumPoolSize()
	 */
	public static int getMaximumPoolSize() {
		return scheduler.getMaximumPoolSize();
	}

	/**
	 * @param time
	 * @param unit
	 * @see java.util.concurrent.ThreadPoolExecutor#setKeepAliveTime(long, java.util.concurrent.TimeUnit)
	 */
	public static void setKeepAliveTime(long time, TimeUnit unit) {
		scheduler.setKeepAliveTime(time, unit);
	}

	/**
	 * @param unit
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getKeepAliveTime(java.util.concurrent.TimeUnit)
	 */
	public static long getKeepAliveTime(TimeUnit unit) {
		return scheduler.getKeepAliveTime(unit);
	}

	/**
	 * @param task
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#remove(java.lang.Runnable)
	 */
	public static boolean remove(Runnable task) {
		return scheduler.remove(task);
	}

	/**
	 * 
	 * @see java.util.concurrent.ThreadPoolExecutor#purge()
	 */
	public static void purge() {
		scheduler.purge();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getPoolSize()
	 */
	public static int getPoolSize() {
		return scheduler.getPoolSize();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getActiveCount()
	 */
	public static int getActiveCount() {
		return scheduler.getActiveCount();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getLargestPoolSize()
	 */
	public static int getLargestPoolSize() {
		return scheduler.getLargestPoolSize();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getTaskCount()
	 */
	public static long getTaskCount() {
		return scheduler.getTaskCount();
	}

	/**
	 * @return
	 * @see java.util.concurrent.ThreadPoolExecutor#getCompletedTaskCount()
	 */
	public static long getCompletedTaskCount() {
		return scheduler.getCompletedTaskCount();
	}

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
