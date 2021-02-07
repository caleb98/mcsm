package ccode.mcsm.action;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

public class GetPermissionLevelAction extends Action {

	public static final String ID = "GetPermissionLevel";
	
	GetPermissionLevelAction() {
		super(ID, Permissions.NO_PERMISSIONS);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		executor.sendMessage(manager, "Your permission level is %s (%s)", 
				executor.getPermissions(), 
				executor.getPermissionsLevel());
		
		if(executor.getOverrideCommands().size() > 0) {
			executor.sendMessage(manager, "You have override permissions for: ");
			for(String override : executor.getOverrideCommands()) {
				executor.sendMessage(manager, " > %s", override);
			}
		}
		
		return 0;
	}
	
}
