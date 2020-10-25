package ccode.mcsm.action;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.scheduling.Scheduler;
import ccode.mcsm.scheduling.TimeFormatters;

/**
 * Schedules an action to be carried out at a specified later date. The 
 * date for this action should follow the format specified by the 
 * <code>DateTimeFormatter.ISO_LOCAL_DATE_TIME</code> formatter.
 */
public class ScheduleAction extends Action {

	public static final String ID = "Schedule";

	private static final Pattern datePattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:.\\d+)?) (\\w+)(.*)");
	
	ScheduleAction() {
		super(ID, 4);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, String args) {
		Matcher m = datePattern.matcher(args);
		
		if(!m.find()) {
			System.err.println("Error in Schedule: provided arguments don't match expected input.");
			return -1;
		}
		
		String date = m.group(1);
		String action = m.group(2);
		String nextArgs = m.group(3).trim();
		
		if(Action.get(action) == null) {
			System.err.println("Error in Schedule: provided action does not exist.");
			return -1;
		}
		
		LocalDateTime scheduleAt;
		try {
			scheduleAt = TimeFormatters.DATE_FORMATTER.parse(date, LocalDateTime::from);
		} catch (DateTimeParseException e) {
			System.err.printf("Error parsing scheduling date: %s\n", e.getMessage());
			return -1;
		}
		long delay = ChronoUnit.SECONDS.between(LocalDateTime.now(), scheduleAt);
		
		if(delay < 0) {
			System.out.println("WARNING: Time for scheduled action has already passed. It will not be executed.");
			return 0;
		}
		
		Scheduler.schedule(
				()->{
					Action.get(action).execute(manager, nextArgs);
				}, 
				delay, 
				TimeUnit.SECONDS
		);
		
		return 0;
	}

}
