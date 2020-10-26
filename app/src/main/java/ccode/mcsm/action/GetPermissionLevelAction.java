package ccode.mcsm.action;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class GetPermissionLevelAction extends Action {

	public static final String ID = "GetPermissionLevel";
	
	GetPermissionLevelAction() {
		super(ID, Permissions.NO_PERMISSIONS);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		sendMessage(manager, executor, "Your permission level is %s (%s)", 
				executor.getPermissions(), 
				executor.getPermissionsLevel());
		
		if(executor.getOverrideCommands().size() > 0) {
			sendMessage(manager, executor, "You have override permissions for: ");
			for(String override : executor.getOverrideCommands()) {
				sendMessage(manager, executor, " > %s", override);
			}
		}
		
		return 0;
	}
	
}
