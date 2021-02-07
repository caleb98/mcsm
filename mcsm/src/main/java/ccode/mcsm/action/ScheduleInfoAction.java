package ccode.mcsm.action;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.scheduling.Schedule;
import ccode.mcsm.scheduling.Scheduler;

public class ScheduleInfoAction extends Action {

	public static final String ID = "ScheduleInfo";
	
	ScheduleInfoAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		Schedule schedule = Scheduler.getSchedule(args);
		if(schedule == null) {
			executor.sendMessage(manager, "Error: invalid schedule id");
			return -1;
		}
		
		executor.sendMessage(manager, "id: %s", schedule.id);
		executor.sendMessage(manager, "executor: %s", schedule.executor.getName());
		
		if(schedule.at == null) {
			long until = schedule.future.getDelay(TimeUnit.of(ChronoUnit.MILLIS));
			executor.sendMessage(manager, "next: %s", 
					LocalTime.now().plus(until, ChronoUnit.MILLIS).truncatedTo(ChronoUnit.SECONDS));
		}
		else {
			executor.sendMessage(manager, "at: %s", schedule.at);
		}
		
		executor.sendMessage(manager, "action: %s", schedule.actionID);
		executor.sendMessage(manager, "arguments: %s", schedule.args);
		
		return 0;
	}
	
}
