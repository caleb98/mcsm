package ccode.mcsm.scheduling;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

import ccode.mcsm.permissions.Player;

public class Schedule {
	
	public final String id;
	public final ScheduledFuture<?> future;
	public final Player executor;
	public final LocalDateTime at;
	public final String actionID;
	public final String args;
	
	public Schedule(String id, ScheduledFuture<?> future, Player executor, LocalDateTime at, String actionID, String args) {
		this.id = id;
		this.future = future;
		this.executor = executor;
		this.at = at;
		this.actionID = actionID;
		this.args = args;
	}

}
