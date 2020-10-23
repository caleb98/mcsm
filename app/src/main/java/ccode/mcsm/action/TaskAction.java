package ccode.mcsm.action;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.task.Tasks;

public class TaskAction extends Action {

	public static final String ID = "Task";
	
	@Override
	public int execute(MinecraftServerManager manager, String task) {
		Tasks.executeTask(task, manager);
		return 0;
	}
	
}
