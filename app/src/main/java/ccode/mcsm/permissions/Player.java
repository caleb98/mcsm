package ccode.mcsm.permissions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ccode.mcsm.action.Action;
import ccode.mcsm.task.Task;

public class Player {
	
	private String name;
	private transient String uuid;
	private Permissions permissions;
	private HashSet<String> commands = new HashSet<>();
	
	public Player(String name, String uuid, Permissions permissions) {
		this.name = name;
		this.uuid = uuid;
		this.permissions = permissions;
	}

	public Set<String> getOverrideCommands() {
		return Collections.unmodifiableSet(commands);
	}
	
	public void setPermissionsLevel(Permissions permissions) {
		this.permissions = permissions;
	}
	
	public Permissions getPermissions() {
		return permissions;
	}
	
	public int getPermissionsLevel() {
		return permissions.level;
	}
	
	public boolean hasPermissions(String actionID) {
		Action action = Action.get(actionID);
		if(action == null) return false;
		return hasPermissions(action);
	}
	
	public boolean hasPermissions(Action action) {
		if(action.requiredPermission.level <= permissions.level) {
			return true;
		}
		for(String permission : commands) {
			if(permission.equals(action.id)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasPermissions(Task task) {
		//TODO: task level overrides
		return task.requiredPermission.level <= permissions.level;
	}
	
	public void addPermission(String permission) {
		commands.add(permission);
	}
	
	public void removePermission(String permission) {
		commands.remove(permission);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
}
