package ccode.mcsm.action;

import java.util.Set;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;
import ccode.mcsm.scheduling.Schedule;
import ccode.mcsm.scheduling.Scheduler;

public class ListSchedulesAction extends Action {

	public static final String ID = "ListSchedules";
	
	public ListSchedulesAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		Set<String> schedules = Scheduler.getSchedules();
		if(schedules.size() == 0) {
			sendMessage(manager, executor, "No active schedules.");
		}
		else {
			sendMessage(manager, executor, "Current Schedules:");
			for(String scheduleID : schedules) {
				Schedule schedule = Scheduler.getSchedule(scheduleID);
				sendMessage(manager, executor, " > %s: %s do %s (%s)", 
						scheduleID, 
						schedule.at, 
						schedule.actionID, 
						schedule.executor.getName());
			}
		}
		return 0;
	}
	
}
