package ccode.mcsm.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ccode.mcsm.MinecraftServerManager;
/**
 * Represents a single action that the server manager may execute.
 * All actions are represented with the format <code>&ltActionID&gt 
 * &ltArguments&gt</code>. The parsing and format of action arguments
 * is dependant upon the action.
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
		map.put(SetPropertyAction.ID, new SetPropertyAction());
		map.put(WaitForAction.ID, new WaitForAction());
		map.put(ScheduleAction.ID, new ScheduleAction());
		map.put(DelayAction.ID, new DelayAction());
		
		actions = Collections.unmodifiableMap(map);
	}
	
	/**
	 * Gets the action ids registered in the Action class static
	 * initializer.
	 * @return a set containing all action ids
	 */
	public static Set<String> getActions() {
		return actions.keySet();
	}
	
	/**
	 * Get's an action using an action id
	 * @param actionID action to retrieve
	 * @return action; null if that action isn't registered/doesn't exist
	 */
	public static Action get(String actionID) {
		return actions.get(actionID);
	}
	
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
