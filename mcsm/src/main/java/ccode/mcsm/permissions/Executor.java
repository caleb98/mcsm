package ccode.mcsm.permissions;

import java.util.Set;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.action.Action;
import ccode.mcsm.task.Task;

public abstract class Executor {
	
	public abstract Set<String> getOverrideCommands();
	public abstract Permissions getPermissions();
	public abstract int getPermissionsLevel();
	public abstract boolean hasPermissions(String actionId);
	public abstract boolean hasPermissions(Action action);
	public abstract boolean hasPermissions(Task task);
	
	public abstract void sendMessage(MinecraftServerManager manager, String message);
	
	public final void sendMessage(MinecraftServerManager manager, String format, Object... args) {
		sendMessage(manager, String.format(format, args));
	}
	
}
