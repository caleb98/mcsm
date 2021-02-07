package ccode.mcsm.permissions;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.action.Action;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.task.Task;

public class Player extends Executor {
	
	private String name;
	private transient String uuid;
	private Permissions permissions;
	private HashSet<String> commands = new HashSet<>();
	private String passwordHash = null;
	
	public Player(String name, String uuid, Permissions permissions) {
		this.name = name;
		this.uuid = uuid;
		this.permissions = permissions;
	}
	
	@Override
	public void sendMessage(MinecraftServerManager manager, String message) {
		if(this == MinecraftServerManager.MCSM_EXECUTOR) {
			System.out.println(message);
		}
		else {
			MinecraftServer server = manager.getServer();
			try {
				server.sendCommand(String.format("tell %s %s", name, message));
			} catch (IOException e) {
				System.err.printf("Error sending message to %s: %s\n", name, message);
			}
		}
	}

	public Set<String> getOverrideCommands() {
		return Collections.unmodifiableSet(commands);
	}
	
	public void setPermissionsLevel(Permissions permissions) {
		this.permissions = permissions;
	}
	
	@Override
	public Permissions getPermissions() {
		return permissions;
	}
	
	@Override
	public int getPermissionsLevel() {
		return permissions.level;
	}
	
	@Override
	public boolean hasPermissions(String actionID) {
		Action action = Action.get(actionID);
		if(action == null) return false;
		return hasPermissions(action);
	}
	
	@Override
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
	
	@Override
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
	
	public String getPasswordHash() {
		return passwordHash;
	}
	
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	
}
