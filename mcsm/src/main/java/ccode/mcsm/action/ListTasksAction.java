package ccode.mcsm.action;

import java.util.ArrayList;
import java.util.Collections;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.task.Tasks;

/**
 * A utility action that prints a list of all the available
 * Tasks to the console.
 */
public class ListTasksAction extends Action {

	public static final String ID = "ListTasks";
	
	ListTasksAction() {
		super(ID, Permissions.LEVEL_0);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		ArrayList<String> tasks = new ArrayList<>(Tasks.getTasks());
		Collections.sort(tasks);
		executor.sendMessage(manager, "Available tasks: ");
		for(String task : tasks) {
			if(executor.hasPermissions(Tasks.getTask(task)))
				executor.sendMessage(manager, " > %s", task);
		}
		return 0;
	}
	
}
