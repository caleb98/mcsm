package ccode.mcsm.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
/**
 * Represents a single action that the server manager may execute.
 * All actions are represented with the format <code>&ltActionID&gt 
 * &ltArguments&gt</code>. The parsing and format of action arguments
 * is dependant upon the action.
 */
public abstract class Action {
	
	public static final Pattern ACTION_COMMAND_PATTERN = Pattern.compile("^(\\w+)(?: ([\\w ]+))?$");
	private static final Map<String, Action> actions = new HashMap<>();
	private static boolean areActionsInitialized = false;
	
	public static void init() {
		
		//Add all actions here
		register(new StartServerAction());
		register(new SaveServerAction());
		register(new StopServerAction());
		register(new SendCommandAction());
		register(new TaskAction());
		register(new ListActionsAction());
		register(new ListTasksAction());
		register(new SetPropertyAction());
		register(new WaitForAction());
		register(new ScheduleAction());
		register(new DelayAction());
	
		areActionsInitialized = true;
		
	}
	
	private static void register(Action action) {
		if(actions.containsKey(action.id)) {
			System.err.printf(
					"Error: attempted to register action %s, which was already "
					+ "registered. Check for action id conflicts.\n", action.id);
			return;
		}
		actions.put(action.id, action);
		System.out.printf("Registered action: \t%s\n", action.id);
	}
	
	public final String id;
	public final Permissions requiredPermission;
	
	public Action(String id, Permissions permissionLevel) {
		this.id = id;
		this.requiredPermission = permissionLevel;
	}
	
	/**
	 * Gets the action ids registered in the Action class static
	 * initializer.
	 * @return a set containing all action ids
	 */
	public static Set<String> getActions() {
		if(!areActionsInitialized) {
			throw new IllegalStateException("Actions not yet initialized");
		}
		return actions.keySet();
	}
	
	/**
	 * Get's an action using an action id
	 * @param actionID action to retrieve
	 * @return action; null if that action isn't registered/doesn't exist
	 */
	public static Action get(String actionID) {
		if(!areActionsInitialized) {
			throw new IllegalStateException("Actions not yet initialized");
		}
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
