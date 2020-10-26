package ccode.mcsm.action;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;
import ccode.mcsm.task.Tasks;

/**
 * An action that runs a task.
 */
public class TaskAction extends Action {

	public static final String ID = "Task";
	
	TaskAction() {
		super(ID, Permissions.LEVEL_4);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String task) {
		Tasks.executeTask(manager, executor, task);
		return 0;
	}
	
}
