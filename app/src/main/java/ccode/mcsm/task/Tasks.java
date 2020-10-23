package ccode.mcsm.task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.action.Action;

public class Tasks {

	private static final String TASKS_FILE = "tasks";
	private static final HashMap<String, String[]> tasks = new HashMap<>();
	
	public static void loadTasks() throws IOException {
		BufferedReader tasksReader = new BufferedReader(new FileReader(TASKS_FILE));
		
		String currentTask = null;
		ArrayList<String> currentTaskActions = new ArrayList<>();
		String line = "\s";
		int lineNo = 1;
		while((line = tasksReader.readLine()) != null) {
			
			//Match for task name header
			if(line.matches("\\[\\w+\\]")) {
				
				//See if we were already processing a task's action list.
				//If so, finalize that task
				if(currentTask != null) {
					if(tasks.containsKey(currentTask)) {
						System.out.printf("WARNING: Task %s overwritten by different task with same name.\n", currentTask);
					}
					tasks.put(currentTask, currentTaskActions.toArray(new String[currentTaskActions.size()]));
					System.out.printf("Loaded task:\t%s\n", currentTask);
				}
				
				//Get new task name and clear actions
				currentTask = line.replaceAll("[\\[\\]]", "");
				currentTaskActions.clear();
				
			}
			
			//Match for task action
			else if(line.matches("\t[\\w\\s]+")) {
				currentTaskActions.add(line.trim());
			}
			
			//Match empty line
			else if(line.matches("[\\s\t]*")) {
				
			}
			else {
				System.out.printf("WARNING: Line %d does not match any expected input in the tasks file. It will not be processed.", lineNo);
			}
			
			lineNo++;
			
		}
		
		//Check for final task
		if(currentTask != null) {
			if(tasks.containsKey(currentTask)) {
				System.out.printf("WARNING: Task %s overwritten by different task with same name.\n", currentTask);
			}
			tasks.put(currentTask, currentTaskActions.toArray(new String[currentTaskActions.size()]));
			System.out.printf("Loaded task:\t%s\n", currentTask);
		}
		
		tasksReader.close();
	}
	
	public static void executeTask(String task, MinecraftServerManager manager) {
		if(tasks.containsKey(task)) {
			for(String fullCommand : tasks.get(task)) {
				String actionID = fullCommand.split("\s+")[0];
				String arguments = fullCommand.substring(actionID.length()).trim();
				Action action = Action.get(actionID);
				if(action != null) {
					Action.get(actionID).execute(manager, arguments);
				}
				else {
					System.err.printf("Error in task \'%s\': listed action \'%s\' does not exist!\n", task, actionID);
				}
			}
		}
		else {
			System.out.printf("Unable to execute task \'%s\': task not found.\n", task);
		}
	}
	
	public static Set<String> getTasks() {
		return tasks.keySet();
	}
	
}
