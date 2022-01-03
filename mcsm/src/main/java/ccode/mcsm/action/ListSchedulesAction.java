package ccode.mcsm.action;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.scheduling.Schedule;
import ccode.mcsm.scheduling.Scheduler;

public class ListSchedulesAction extends Action {

	public static final String ID = "ListSchedules";
	
	public ListSchedulesAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		Set<String> schedules = Scheduler.getSchedules();
		if(schedules.size() == 0) {
			executor.sendMessage(manager, "No active schedules.");
		}
		else {			
			executor.sendMessage(manager, "Current Schedules:");
			for(String scheduleID : schedules) {
				Schedule schedule = Scheduler.getSchedule(scheduleID);
				
				String time;
				
				if(schedule.at != null) {
					time = schedule.at.toString();
				}
				else {
					time = LocalTime.now().plus(schedule.future.getDelay(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS)
							.truncatedTo(ChronoUnit.SECONDS).toString();
				}
				
				executor.sendMessage(manager, " > %s: %s do %s (%s)", 
						scheduleID, 
						time,
						schedule.actionID, 
						schedule.executor.getName());
			}
		}
		return 0;
	}
	
}
