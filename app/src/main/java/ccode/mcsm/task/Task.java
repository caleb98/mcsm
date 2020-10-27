package ccode.mcsm.task;

import java.util.Collection;

import ccode.mcsm.action.Action;
import ccode.mcsm.permissions.Permissions;

public class Task {

	public final String[] actions;
	public final Permissions requiredPermission;
	
	/**
	 * Creates a new task from the specified collection of 
	 * commands. This constructor will automatically set
	 * the permissions level required to run this task as
	 * the highest required level of all actions in the task.
	 * @param actions actions to run for this task
	 */
	Task(Collection<String> actions) {
		this.actions = actions.toArray(new String[actions.size()]);
		Permissions maxPermission = Permissions.NO_PERMISSIONS;
		for(String a : this.actions) {
			String actionID = a.split("\s+")[0];
			Action action = Action.get(actionID);
			if(action != null && action.requiredPermission.level > maxPermission.level) {
				maxPermission = action.requiredPermission;
			}
		}
		requiredPermission = maxPermission;
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
	Task(Collection<String> actions, Permissions permissionsRequired) {
		this.actions = actions.toArray(new String[actions.size()]);
		if(permissionsRequired == null) {
			Permissions maxPermission = Permissions.NO_PERMISSIONS;
			for(String a : this.actions) {
				String actionID = a.split("\s+")[0];
				Action action = Action.get(actionID);
				if(action != null && action.requiredPermission.level > maxPermission.level) {
					maxPermission = action.requiredPermission;
				}
			}
			this.requiredPermission = maxPermission;
		}
		else {
			this.requiredPermission = permissionsRequired;
		}
	}
	
}
