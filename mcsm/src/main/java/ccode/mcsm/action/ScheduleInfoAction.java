package ccode.mcsm.action;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;
import ccode.mcsm.scheduling.Schedule;
import ccode.mcsm.scheduling.Scheduler;

public class ScheduleInfoAction extends Action {

	public static final String ID = "ScheduleInfo";
	
	ScheduleInfoAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		Schedule schedule = Scheduler.getSchedule(args);
		if(schedule == null) {
			sendMessage(manager, executor, "Error: invalid schedule id");
			return -1;
		}
		
		sendMessage(manager, executor, "id: %s", schedule.id);
		sendMessage(manager, executor, "executor: %s", schedule.executor.getName());
		
		if(schedule.at == null) {
			long until = schedule.future.getDelay(TimeUnit.of(ChronoUnit.MILLIS));
			sendMessage(manager, executor, "next: %s", 
					LocalTime.now().plus(until, ChronoUnit.MILLIS).truncatedTo(ChronoUnit.SECONDS));
		}
		else {
			sendMessage(manager, executor, "at: %s", schedule.at);
		}
		
		sendMessage(manager, executor, "action: %s", schedule.actionID);
		sendMessage(manager, executor, "arguments: %s", schedule.args);
		
		return 0;
	}
	
}
