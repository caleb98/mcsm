package ccode.mcsm.action;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;
import ccode.mcsm.permissions.RemoteExecutor;
import ccode.mcsm.scheduling.Schedule;
import ccode.mcsm.scheduling.Scheduler;

/**
 * Schedules an action to be carried out at a specified later date. The 
 * date for this action should follow the format specified by the 
 * <code>DateTimeFormatter.ISO_LOCAL_DATE_TIME</code> formatter.
 */
public class ScheduleAction extends Action {

	public static final String ID = "Schedule";

	public static final Pattern ARGUMENT_PATTERN = Pattern.compile("(\\w+\\s+)?([HD])?([\\d-T:.]+) (\\w+)(.*)");
	
	private static final Pattern DATE_TIME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d{1,9})?)?");
	private static final Pattern TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d{1,9})?)?");
	
	private static final Pattern HOURLY_TASK_TIME = Pattern.compile("\\d{2}:\\d{2}");
	private static final long HOUR_MILLIS = 60 * 60 * 1000;
	private static final Pattern DAILY_TASK_TIME = Pattern.compile("\\d{2}:\\d{2}:\\d{2}");
	private static final long DAY_MILLIS = HOUR_MILLIS * 24;
	
	ScheduleAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		Matcher m = ARGUMENT_PATTERN.matcher(args);
		
		if(!m.matches()) {
			executor.sendMessage(manager, "Error in Schedule: provided arguments don't match expected input.");
			return -1;
		}
		
		String scheduleID = m.group(1);
		String frequency = m.group(2);
		String time = m.group(3);
		String actionID = m.group(4);
		String nextArgs = m.group(5).trim();
		
		if(scheduleID != null) scheduleID = scheduleID.trim();
		
		if(Action.get(actionID) == null) {
			executor.sendMessage(manager, "Error in Schedule: provided action does not exist.");
			return -1;
		}
		
		//See if we're scheduling a single action or a repeated action
		if(frequency != null) {
			
			//Scheduling a repeated (hourly/daily) action
			if(frequency.equals("H")) {
				
				Matcher freqMatcher = HOURLY_TASK_TIME.matcher(time);
				if(!freqMatcher.matches()) {
					executor.sendMessage(manager, "Provided schedule time is incorrect. Expected format: MM:SS");
					return -1;
				}
				
				LocalTime now = LocalTime.now();
				String fullTime = String.format("%02d:%s", now.getHour(), time);
				LocalTime executeAt = LocalTime.parse(fullTime);
				
				if(executeAt.isBefore(now)) {
					executeAt = executeAt.plusHours(1);
				}
				
				long delayMillis = ChronoUnit.MILLIS.between(now, executeAt);
				
				if(scheduleID == null)
					scheduleID = "Hourly-" + getScheduleUUID();
				
				ScheduledFuture<?> future = Scheduler.scheduleAtFixedRate(
						()->{
							Action.get(actionID).execute(manager, executor, nextArgs);
						}, 
						delayMillis, 
						HOUR_MILLIS, 
						TimeUnit.of(ChronoUnit.MILLIS)
				);
				
				Player playerExec;
				if(executor instanceof RemoteExecutor) {
					RemoteExecutor re = (RemoteExecutor) executor;
					playerExec = re.getPlayer();
				}
				else if(executor instanceof Player) {
					playerExec = (Player) executor;
				}
				else {
					executor.sendMessage(manager, "Error resolving executor type for schedule. Schedule will not be set.");
					return -1;
				}
				Schedule schedule = new Schedule(scheduleID, future, playerExec, null, actionID, nextArgs);
				Scheduler.registerSchedule(scheduleID, schedule);
				
				executor.sendMessage(manager, "Registered schedule: %s", scheduleID);
				return 0;
				
			}
			else if(frequency.equals("D")) {
				
				Matcher freqMatcher = DAILY_TASK_TIME.matcher(time);
				if(!freqMatcher.matches()) {
					executor.sendMessage(manager, "Provided schedule time is incorrect. Expected format: HH:MM:SS");
					return -1;
				}
				
				LocalTime now = LocalTime.now();
				LocalTime executeAt = LocalTime.parse(time);
				long delayMillis = (ChronoUnit.MILLIS.between(now, executeAt) + DAY_MILLIS) % DAY_MILLIS;
				
				if(scheduleID == null) 
					scheduleID = "Daily-" + getScheduleUUID();
				
				ScheduledFuture<?> future = Scheduler.scheduleAtFixedRate(
						()->{
							Action.get(actionID).execute(manager, executor, nextArgs);
						}, 
						delayMillis, 
						DAY_MILLIS, 
						TimeUnit.of(ChronoUnit.MILLIS)
				);
				
				Player playerExec;
				if(executor instanceof RemoteExecutor) {
					RemoteExecutor re = (RemoteExecutor) executor;
					playerExec = re.getPlayer();
				}
				else if(executor instanceof Player) {
					playerExec = (Player) executor;
				}
				else {
					executor.sendMessage(manager, "Error resolving executor type for schedule. Schedule will not be set.");
					return -1;
				}
				Schedule schedule = new Schedule(scheduleID, future, playerExec, null, actionID, nextArgs);
				Scheduler.registerSchedule(scheduleID, schedule);
				
				executor.sendMessage(manager, "Registered schedule: %s", scheduleID);
				return 0;
				
			}
			else {
				executor.sendMessage(manager, "Error processing schedule frequency.");
				return -1;
			}
		}
		else {
			
			//Scheduling a one-off action
			Matcher dateTimeMatcher = DATE_TIME_PATTERN.matcher(time);
			Matcher timeMatcher = TIME_PATTERN.matcher(time);
			LocalDateTime scheduleAt;
			
			if(dateTimeMatcher.matches()) {
				scheduleAt = DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(time, LocalDateTime::from);
			}
			else if(timeMatcher.matches()) {
				//Only time was given, so we need to see if that time was before or after now
				LocalTime scheduleTime = DateTimeFormatter.ISO_LOCAL_TIME.parse(time, LocalTime::from);
				LocalDate today = LocalDate.now();
				
				LocalDateTime now = LocalDateTime.now();
				scheduleAt = scheduleTime.atDate(today);
				
				double diff = ChronoUnit.MILLIS.between(now, scheduleAt);
				
				//Time already happened today, so schedule for tomorrow
				if(diff < 0) {
					scheduleAt = scheduleTime.atDate(today.plusDays(1));
				}
			}
			else {
				executor.sendMessage(manager, "Error in Schedule: provided date/time %s is not a valid format", time);
				return -1;
			}

			long delayMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), scheduleAt);
			
			if(delayMillis < 0) {
				executor.sendMessage(manager, "WARNING: Time for scheduled action has already passed. It will not be executed.");
				return 0;
			}
			
			if(scheduleID == null) 
				scheduleID = getScheduleUUID();
			
			ScheduledFuture<?> future = Scheduler.schedule(
					()->{
						Action.get(actionID).execute(manager, executor, nextArgs);
					}, 
					delayMillis, 
					TimeUnit.of(ChronoUnit.MILLIS)
			);
			
			Player playerExec;
			if(executor instanceof RemoteExecutor) {
				RemoteExecutor re = (RemoteExecutor) executor;
				playerExec = re.getPlayer();
			}
			else if(executor instanceof Player) {
				playerExec = (Player) executor;
			}
			else {
				executor.sendMessage(manager, "Error resolving executor type for schedule. Schedule will not be set.");
				return -1;
			}
			Schedule schedule = new Schedule(scheduleID, future, playerExec, scheduleAt, actionID, nextArgs);
			Scheduler.registerSchedule(scheduleID, schedule);
			executor.sendMessage(manager, "Registered schedule: %s", scheduleID);
			
			return 0;
			
		}
	}
	
	private static String getScheduleUUID() {
		return UUID.randomUUID().toString().substring(0, 5);
	}

}
