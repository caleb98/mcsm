package ccode.mcsm.task;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.action.Action;
import ccode.mcsm.action.TaskAction;
import ccode.mcsm.permissions.Permissions;

public class Task {

	private static final Pattern ARGUMENT = Pattern.compile("\\$(\\d+)");
	
	public final String[] actions;
	public final int argc;
	
	private final Permissions permissionOverride;
	
	/**
	 * Creates a new task from the specified collection of 
	 * commands. This constructor will automatically set
	 * the permissions level required to run this task as
	 * the highest required level of all actions in the task.
	 * @param actions actions to run for this task
	 */
	Task(Collection<String> actions) {
		this(actions, null);
	}
	
	/**
	 * Creates a new task from the specified collection of
	 * commands with the given permissions level requirement.
	 * If {@code permissionsRequired} is null, then this method
	 * will automatically set the required permission level
	 * to the highest required level of all the actions in
	 * the task.
	 * @param actions actions to run for this task
	 * @param permissionsRequired min permission level to execute this task
	 */
	Task(Collection<String> actions, Permissions override) {
		this.actions = actions.toArray(new String[actions.size()]);
		this.permissionOverride = override;
		
		//Find all our argument strings
		int maxArg = -1;
		for(String action : actions) {
			Matcher argFinder = ARGUMENT.matcher(action);
			while(argFinder.find()) {
				int argNum = Integer.valueOf(argFinder.group(1));
				if(argNum > maxArg) {
					maxArg = argNum;
				}
			}
		}
		argc = maxArg + 1;
	}
	
	public Permissions getRequiredPermissions() {
		// If an override is specified, return that
		if(permissionOverride != null) {
			return permissionOverride;
		}
		
		// Otherwise, calculate based on the called actions
		Permissions maxPermission = Permissions.NO_PERMISSIONS;
		for(String a : this.actions) {
			String actionID = a.split("\s+")[0];
			Action action = Action.get(actionID);
			
			// Ignore invalid actions - they'll throw their error later
			if(action == null) {
				continue;
			}
			
			// Check task permissions differently
			else if(action.id.equals(TaskAction.ID)) {
				String taskName = a.substring(actionID.length()).trim().split("\\s+")[0];
				Task t = Tasks.getTask(taskName);
				if(t == null) {
					continue;
				}
				Permissions taskRequires = t.getRequiredPermissions();
				if(taskRequires.level > maxPermission.level) {
					maxPermission = taskRequires;
				}
			}
			
			// Regular action, just take its permissions
			else if(action.requiredPermission.level > maxPermission.level) {
				maxPermission = action.requiredPermission;
			}
		}
		return maxPermission;
	}
	
}
