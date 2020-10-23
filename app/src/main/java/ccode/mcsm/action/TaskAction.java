package ccode.mcsm.action;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.task.Tasks;

/**
 * An action that runs a task.
 */
public class TaskAction extends Action {

	public static final String ID = "Task";
	
	@Override
	public int execute(MinecraftServerManager manager, String task) {
		Tasks.executeTask(task, manager);
		return 0;
	}
	
}
