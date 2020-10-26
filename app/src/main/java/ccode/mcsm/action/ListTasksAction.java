package ccode.mcsm.action;

import java.util.ArrayList;
import java.util.Collections;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;
import ccode.mcsm.task.Tasks;

/**
 * A utility action that prints a list of all the available
 * Tasks to the console.
 */
public class ListTasksAction extends Action {

	public static final String ID = "ListTasks";
	
	ListTasksAction() {
		super(ID, Permissions.LEVEL_4);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		ArrayList<String> tasks = new ArrayList<>(Tasks.getTasks());
		Collections.sort(tasks);
		//TODO: task permission levels
		System.out.println("Available tasks: ");
		for(String task : tasks) {
			System.out.printf("\t%s\n", task);
		}
		return 0;
	}
	
}
