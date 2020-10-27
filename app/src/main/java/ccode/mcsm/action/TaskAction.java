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
	public int execute(MinecraftServerManager manager, Player executor, String taskCommand) {
		String[] split = taskCommand.split("\\s+");
		
		//Make sure a task name was provided
		if(split.length == 0) {
			sendMessage(manager, executor, "Error: no task name provided.");
			return -1;
		}
		
		//Grab task name
		String taskName = split[0];
		String taskArgs = "";
		
		//If our split string has more than one value, that means
		//that we had arguments to provide to the task. Find that 
		//args string.
		if(split.length > 1) {
			taskArgs = taskCommand.substring(taskName.length()).trim();
		}
		
		Tasks.executeTask(manager, executor, taskName, taskArgs);
		return 0;
	}
	
}
