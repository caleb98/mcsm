package ccode.mcsm.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.action.Action;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

public class Tasks {

	private static final Pattern TASK_DEFINITION;
	private static final Pattern TASK_ACTION = Pattern.compile("\t[\\w]+.*");
	private static final Pattern EMPTY_LINE = Pattern.compile("[\\s\t]*");
	private static final Pattern TASK_ARGS = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
	private static final String TASKS_FILE_NAME = "mcsm_tasks";
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
	
	public static void loadTasks(File dataDirectory) throws IOException {
		//If the tasks file doesn't exists, go ahead and create it
		File tasksFile = new File(dataDirectory, TASKS_FILE_NAME);
		if(!tasksFile.exists()) {
			tasksFile.createNewFile();
		}
		
		//Load custom tasks
		InputStream customTasksInputStream = new FileInputStream(tasksFile);
		loadTasksFromInputStream(customTasksInputStream);
	}
	
	private static void loadTasksFromInputStream(InputStream stream) throws IOException {
		BufferedReader tasksReader = new BufferedReader(new InputStreamReader(stream));
		
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
					registerTask(currentTask, currentPermissions, currentTaskActions);
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
				System.out.printf("WARNING: Line %d does not match any expected input in the tasks file. It will be ignored.\n", lineNo);
			}
			
		}
		
		//Check for final task
		if(currentTask != null) {
			registerTask(currentTask, currentPermissions, currentTaskActions);
		}
		
		for(String taskName : tasks.keySet()) {
			Task t = tasks.get(taskName);
			System.out.printf("Loaded task:\t%s (%s, argc=%d)\n", 
					taskName, t.getRequiredPermissions(), t.argc);
		}
		
		tasksReader.close();
	}
	
	public static void registerTask(String taskID, String permissions, Collection<String> actions) {
		if(tasks.containsKey(taskID)) {
			System.out.printf("WARNING: Task %s overwritten by different task with same name.\n", taskID);
		}
		Permissions permission = null;
		try {
			permission = Permissions.valueOf(permissions);
		} catch (IllegalArgumentException | NullPointerException e) {}
		Task task = new Task(actions, permission);
		tasks.put(taskID, task);
	}
	
	public static Task getTask(String task) {
		return tasks.get(task);
	}
	
	public static void executeTask(MinecraftServerManager manager, Executor executor, String taskID, String taskArgsString) {
		if(tasks.containsKey(taskID)) {
			Task task = tasks.get(taskID);
			
			System.out.printf("Running task %s with args: \"%s\"\n", taskID, taskArgsString);
			
			//Check executor permissions
			if(!executor.hasPermissions(task)) {
				executor.sendMessage(manager, "You don't have the required permissions to run task %s", taskID);
				return;
			}
			
			//Split up the arguments
			Matcher taskArgsFinder = TASK_ARGS.matcher(taskArgsString);
			ArrayList<String> taskArgs = new ArrayList<>();
			while(taskArgsFinder.find()) {
				taskArgs.add(taskArgsFinder.group(1).replace("\"", ""));
			}
			
			//Make sure we have enough arguments for the task
			if(taskArgs.size() != task.argc) {
				executor.sendMessage(manager, "Error: invalid number of arguments provided for task %s (%d given, expected %d)",
						taskID, taskArgs.size(), task.argc);
				return;
			}
			
			Thread taskThread = new Thread(()->{
				for(String fullCommand : task.actions) {
					
					//Get the action to run
					String actionID = fullCommand.split("\s+")[0];
					Action action = Action.get(actionID);
					
					//Get action arguments
					String arguments = fullCommand.substring(actionID.length()).trim();
					String replString;
					for(int i = 0; i < taskArgs.size(); i++) {
						replString = String.format("\\$%d", i);
						arguments = arguments.replaceAll(replString, taskArgs.get(i));
					}
					
					if(action != null) {
						int result = Action.get(actionID).execute(manager, executor, arguments);
						if(result < 0) {
							executor.sendMessage(manager, "Action %s failed (%d); task %s stopping.", actionID, result, taskID);
							break;
						}
					}
					else {
						executor.sendMessage(manager, "Error in task \'%s\': listed action \'%s\' does not exist!", taskID, actionID);
					}
					
				}
			}, "Task-" + taskCount++ + "-" + taskID);
			taskThread.setDaemon(true);
			taskThread.start();
		}
		else {
			executor.sendMessage(manager, "Unable to execute task \'%s\': task not found.", taskID);
		}
	}
	
	public static Set<String> getTasks() {
		return tasks.keySet();
	}
	
}
