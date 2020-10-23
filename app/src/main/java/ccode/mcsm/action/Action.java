package ccode.mcsm.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ccode.mcsm.MinecraftServerManager;
/**
 * Represents a single action that the server manager may execute.
 */
public abstract class Action {
	
	private static final Map<String, Action> actions;
	
	static {
		HashMap<String, Action> map = new HashMap<>();
		
		//Add all actions here
		map.put(StartServerAction.ID, new StartServerAction());
		map.put(SaveServerAction.ID, new SaveServerAction());
		map.put(StopServerAction.ID, new StopServerAction());
		map.put(SendCommandAction.ID, new SendCommandAction());
		map.put(TaskAction.ID, new TaskAction());
		map.put(ListActionsAction.ID, new ListActionsAction());
		map.put(ListTasksAction.ID, new ListTasksAction());
		
		actions = Collections.unmodifiableMap(map);
	}
	
	public static Set<String> getActions() {
		return actions.keySet();
	}
	
	public static Action get(String actionID) {
		return actions.get(actionID);
	}

	Action() {}
	
	/**
	 * Runs this action.
	 * @param manager the manager that should execute the action
	 * @param args the string of arguments for this action to run
	 * @return 0 for success; non-zero value for error
	 */
	public abstract int execute(MinecraftServerManager manager, String args);
	
	/**
	 * Runs this action. Uses an empty string as the arguments.
	 * @param manager the manager that should execute the action
	 * @return 0 for success; non-zero value for error
	 */
	public final int execute(MinecraftServerManager manager) {
		return execute(manager, "");
	}
	
}
