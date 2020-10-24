package ccode.mcsm.permissions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Player {

	private String name;
	private transient String uuid;
	private HashSet<String> commands = new HashSet<>();
	
	public Player(String name, String uuid) {
		this.name = name;
		this.uuid = uuid;
	}

	public Set<String> getOverrideCommands() {
		return Collections.unmodifiableSet(commands);
	}
	
	public boolean hasPermissions(String action) {
		for(String permission : commands) {
			if(permission.equals(action)) {
				return true;
			}
		}
		return false;
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
