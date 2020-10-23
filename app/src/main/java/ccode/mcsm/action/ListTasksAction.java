package ccode.mcsm.action;

import java.util.ArrayList;
import java.util.Collections;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.task.Tasks;

/**
 * A utility action that prints a list of all the available
 * Tasks to the console.
 */
public class ListTasksAction extends Action {

	public static final String ID = "ListTasks";
	
	@Override
	public int execute(MinecraftServerManager manager, String args) {
		ArrayList<String> tasks = new ArrayList<>(Tasks.getTasks());
		Collections.sort(tasks);
		
		System.out.println("Available tasks: ");
		for(String task : tasks) {
			System.out.printf("\t%s\n", task);
		}
		return 0;
	}
	
}
