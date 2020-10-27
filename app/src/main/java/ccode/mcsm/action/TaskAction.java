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
		//Tasks require their own permissions, so set the Task action
		//permission level to 0.
		super(ID, Permissions.LEVEL_0);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String task) {
		Tasks.executeTask(manager, executor, task);
		return 0;
	}
	
}
