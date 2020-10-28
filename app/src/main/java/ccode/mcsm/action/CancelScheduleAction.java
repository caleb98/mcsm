package ccode.mcsm.action;

import java.util.Set;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;
import ccode.mcsm.scheduling.Scheduler;

public class CancelScheduleAction extends Action {

	public static final String ID = "CancelSchedule";
	
	public CancelScheduleAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		Set<String> schedules = Scheduler.getSchedules();
		if(!schedules.contains(args)) {
			sendMessage(manager, executor, "Error: schedule not found.");
			return -1;
		}
		
		Scheduler.cancelSchedule(args);
		sendMessage(manager, executor, "Canceled schedule %s", args);
		return 0;
	}
	
}
