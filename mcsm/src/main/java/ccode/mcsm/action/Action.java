package ccode.mcsm.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;
/**
 * Represents a single action that the server manager may execute.
 * All actions are represented with the format <code>&ltActionID&gt 
 * &ltArguments&gt</code>. The parsing and format of action arguments
 * is dependant upon the action.
 */
public abstract class Action {
	
	public static final Pattern ACTION_COMMAND_PATTERN = Pattern.compile("^(\\w+)(?: (.+))?$");
	private static final Map<String, Action> actions = new HashMap<>();
	private static boolean areActionsInitialized = false;
	private static int asyncActionCount = 0;
	
	public static void init() {
		
		//Add all actions here
		register(new StartServerAction());
		register(new SaveServerAction());
		register(new StopServerAction());
		register(new SendCommandAction());
		register(new SetPropertyAction());
		
		register(new BackupWorldAction());
		register(new ListBackupPoliciesAction());
		register(new BackupPolicyInfoAction());
		register(new SetBackupPolicyAction());
		register(new ListBackupsAction());
		register(new RestoreBackupAction());
		
		register(new TaskAction());
		register(new ListActionsAction());
		register(new ListTasksAction());
		
		register(new WaitForAction());
		register(new DelayAction());
		
		register(new SetPermissionLevelAction());
		register(new GetPermissionLevelAction());
		register(new GrantUserPermissionAction());
		register(new RevokeUserPermissionAction());
		
		register(new ScheduleAction());
		register(new ListSchedulesAction());
		register(new CancelScheduleAction());
		register(new ScheduleInfoAction());
		
		register(new EnablePVPAction());
		register(new DisablePVPAction());
		
		register(new SetWorldAction());
		register(new SetSeedAction());
		register(new ListWorldsAction());
		
		register(new NewPasswordAction());
	
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
		System.out.printf("Registered action: \t%s\t(%s)\n", action.id, action.requiredPermission);
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
	 * Runs a given task asynchronously. This method <b>DOES NOT</b>
	 * check the executor's permissions, so that check must be done manually beforehand!
	 * @param actionID id of task to run
	 * @param manager 
	 * @param executor the player who executed the action
	 * @param args arguments for the action
	 */
	public static void runAsync(String actionID, MinecraftServerManager manager, Executor executor, String args) {
		final Action action = actions.get(actionID);
		final String trimmedArgs = args.trim();
		if(action != null) {
			//Tasks run asynchronously anyway, so just run the task
			//and let it handle the ansync part by itself.
			if(actionID.equals(TaskAction.ID)) {
				action.execute(manager, executor, trimmedArgs);
			}
			//Not a task action, so make the thread and run
			else {
				Thread actionThread = new Thread(()->{
					action.execute(manager, executor, trimmedArgs);
				}, "AsyncAction-" + asyncActionCount++ + "-" + actionID);
				actionThread.setDaemon(true);
				actionThread.start();
			}
		}
	}
	
	public abstract int execute(MinecraftServerManager manager, Executor executor, String args);
	
}
