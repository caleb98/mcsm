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
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;
import ccode.mcsm.scheduling.Schedule;
import ccode.mcsm.scheduling.Scheduler;

/**
 * Schedules an action to be carried out at a specified later date. The 
 * date for this action should follow the format specified by the 
 * <code>DateTimeFormatter.ISO_LOCAL_DATE_TIME</code> formatter.
 */
public class ScheduleAction extends Action {

	public static final String ID = "Schedule";

	private static final Pattern DATE_TIME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d{1,9})?)?");
	private static final Pattern TIME_PATTERN = Pattern.compile("\\d{2}:\\d{2}(?::\\d{2}(?:\\.\\d{1,9})?)?");
	private static final Pattern ARGS_PATTERN = Pattern.compile("([\\d-T:.]+) (\\w+)(.*)");
	
	ScheduleAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		Matcher m = ARGS_PATTERN.matcher(args);
		
		if(!m.matches()) {
			sendMessage(manager, executor, "Error in Schedule: provided arguments don't match expected input.");
			return -1;
		}
		
		String date = m.group(1);
		String actionID = m.group(2);
		String nextArgs = m.group(3).trim();
		
		if(Action.get(actionID) == null) {
			sendMessage(manager, executor, "Error in Schedule: provided action does not exist.");
			return -1;
		}
		
		Matcher dateTimeMatcher = DATE_TIME_PATTERN.matcher(date);
		Matcher timeMatcher = TIME_PATTERN.matcher(date);
		LocalDateTime scheduleAt;
		if(dateTimeMatcher.matches()) {
			scheduleAt = DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(date, LocalDateTime::from);
		}
		else if(timeMatcher.matches()) {
			//Only time was given, so we need to see if that time was before or after now
			LocalTime scheduleTime = DateTimeFormatter.ISO_LOCAL_TIME.parse(date, LocalTime::from);
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
			sendMessage(manager, executor, "Error in Schedule: provided date/time %s is not a valid format", date);
			return -1;
		}

		long delayMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), scheduleAt);
		
		if(delayMillis < 0) {
			sendMessage(manager, executor, "WARNING: Time for scheduled action has already passed. It will not be executed.");
			return 0;
		}
		
		String scheduleID = UUID.randomUUID().toString().substring(0, 5);
		ScheduledFuture<?> future = Scheduler.schedule(
				()->{
					Action.get(actionID).execute(manager, executor, nextArgs);
				}, 
				delayMillis, 
				TimeUnit.of(ChronoUnit.MILLIS)
		);
		Schedule schedule = new Schedule(scheduleID, future, executor, scheduleAt, actionID, nextArgs);
		Scheduler.registerSchedule(scheduleID, schedule);
		sendMessage(manager, executor, "Registered schedule: %s", scheduleID);
		
		return 0;
	}

}
