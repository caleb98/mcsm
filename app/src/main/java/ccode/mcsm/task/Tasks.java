package ccode.mcsm.task;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.action.Action;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class Tasks {

	private static final Pattern TASK_DEFINITION;
	private static final Pattern TASK_ACTION = Pattern.compile("\t[\\w\\s\\$]+");
	private static final Pattern EMPTY_LINE = Pattern.compile("[\\s\t]*");
	private static final Pattern HAS_ARGS = Pattern.compile("\\$\\d");
	private static final Pattern TASK_ARGS = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
	private static final String TASKS_FILE = "tasks";
	private static final HashMap<String, Task> tasks = new HashMap<>();
	private static int taskCount = 0;
	
	static {
		StringBuilder permissions = new StringBuilder();
		for(int i = 0; i < Permissions.values().length; ++i) {
			permissions.append(Permissions.values()[i]);
			if(i != Permissions.values().length - 1) {
				permissions.append("|");
			}
		}
		String regex = String.format("(\\w+):[\\s\t]*(%s)?", permissions);
		TASK_DEFINITION = Pattern.compile(regex);
	}
	
	public static void loadTasks() throws IOException {
		BufferedReader tasksReader = new BufferedReader(new FileReader(TASKS_FILE));
		
		String currentTask = null;
		String currentPermissions = null;
		ArrayList<String> currentTaskActions = new ArrayList<>();
		int lineNo = 0;
		
		String line;
		while((line = tasksReader.readLine()) != null) {
			
			lineNo++;
			
			//Match for task name header
			Matcher taskDef = TASK_DEFINITION.matcher(line);
			if(taskDef.matches()) {
				
				//See if we were already processing a task's action list.
				//If so, finalize that task
				if(currentTask != null) {
					if(tasks.containsKey(currentTask)) {
						System.out.printf("WARNING: Task %s overwritten by different task with same name.\n", currentTask);
					}
					Permissions permission = null;
					try {
						permission = Permissions.valueOf(currentPermissions);
					} catch (IllegalArgumentException | NullPointerException e) {}
					Task loaded = new Task(currentTaskActions, permission);
					tasks.put(currentTask, loaded);
					System.out.printf("Loaded task:\t%s (%s)\n", currentTask, loaded.requiredPermission);
				}
				
				//Get new task name and clear actions
				currentTask = taskDef.group(1);
				currentPermissions = taskDef.group(2);
				currentTaskActions.clear();
				continue;
				
			}
			
			//Match for task action
			Matcher taskAct = TASK_ACTION.matcher(line);
			if(taskAct.matches()) {
				currentTaskActions.add(line.trim());
				continue;
			}
			
			//Match empty line
			Matcher empty = EMPTY_LINE.matcher(line);
			if(empty.matches()) {
				continue;
			}
			else {
				System.out.printf("WARNING: Line %d does not match any expected input in the tasks file. It will be ignored.", lineNo);
			}
			
		}
		
		//Check for final task
		if(currentTask != null) {
			if(tasks.containsKey(currentTask)) {
				System.out.printf("WARNING: Task %s overwritten by different task with same name.\n", currentTask);
			}
			Permissions permission = null;
			try {
				permission = Permissions.valueOf(currentPermissions);
			} catch (IllegalArgumentException | NullPointerException e) {}
			Task loaded = new Task(currentTaskActions, permission);
			tasks.put(currentTask, loaded);
			System.out.printf("Loaded task:\t%s (%s)\n", currentTask, loaded.requiredPermission);
		}
		
		tasksReader.close();
	}
	
	public static void executeTask(MinecraftServerManager manager, Player executor, String taskID, String taskArgsString) {
		if(tasks.containsKey(taskID)) {
			Task task = tasks.get(taskID);
			
			System.out.printf("Running task %s with args: \"%s\"\n", taskID, taskArgsString);
			
			//Check executor permissions
			if(!executor.hasPermissions(task)) {
				Action.sendMessage(manager, executor, "You don't have the required permissions to run task %s", taskID);
				return;
			}
			
			//Split up the arguments
			Matcher taskArgsFinder = TASK_ARGS.matcher(taskArgsString);
			ArrayList<String> taskArgs = new ArrayList<>();
			while(taskArgsFinder.find()) {
				taskArgs.add(taskArgsFinder.group(1).replace("\"", ""));
			}
			
			Thread taskThread = new Thread(()->{
				for(String fullCommand : task.actions) {
					
					//Get the action to run
					String actionID = fullCommand.split("\s+")[0];
					Action action = Action.get(actionID);
					
					//Get action arguments
					String arguments = fullCommand.substring(actionID.length()).trim();
					String replString;
					String next;
					int i = 0;
					while(i < taskArgs.size()) {
						replString = String.format("\\$%d", i);
						next = arguments.replaceAll(replString, taskArgs.get(i));
						if(!next.equals(arguments))
							arguments = next;
						else 
							break;
					}
					
					Matcher hasArgs = HAS_ARGS.matcher(arguments);
					if(hasArgs.find()) {
						Action.sendMessage(manager, executor, "Error: not enough arguments provided for task.");
						break;
					}
					
					if(action != null) {
						int result = Action.get(actionID).execute(manager, executor, arguments);
						if(result < 0) {
							Action.sendMessage(manager, executor, "Action %s failed (%d); task %s stopping.", actionID, result, taskID);
							break;
						}
					}
					else {
						Action.sendMessage(manager, executor, "Error in task \'%s\': listed action \'%s\' does not exist!", taskID, actionID);
					}
					
				}
			}, "Task-" + taskCount++ + "-" + taskID);
			taskThread.setDaemon(true);
			taskThread.start();
		}
		else {
			Action.sendMessage(manager, executor, "Unable to execute task \'%s\': task not found.", taskID);
		}
	}
	
	public static Set<String> getTasks() {
		return tasks.keySet();
	}
	
}
