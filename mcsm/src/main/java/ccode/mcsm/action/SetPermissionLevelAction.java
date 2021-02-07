package ccode.mcsm.action;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class SetPermissionLevelAction extends Action {

	public static final String ID = "SetPermissionLevel";
	
	SetPermissionLevelAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}

	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		String[] split = args.split("\s+");
		if(split.length != 2) {
			executor.sendMessage(manager, "Error: invalid number of arguments for SetPermissionsLevel");
			return -1;
		}
		
		Player changing = manager.getPlayerFromName(split[0]);
		if(changing == null) {
			executor.sendMessage(manager, "Error: invalid player name");
			return -1;
		}
		
		Permissions newPermissions;
		try {
			newPermissions = Permissions.valueOf(split[1]);
		} catch (IllegalArgumentException e) {
			executor.sendMessage(manager, "Error: invalid permissions string");
			return -1;
		}
		
		//TODO: permission rank, where people who have had a rank longer can't change
		if(executor.getPermissionsLevel() <= changing.getPermissionsLevel()) {
			executor.sendMessage(manager, "Error: that player's current permissions level is too high for you to change");
			return -1;
		}
		if(executor.getPermissionsLevel() <= newPermissions.level) {
			executor.sendMessage(manager, "Error: you cannot set permissions level equal or higher than your current permission level");
			return -1;
		}
		if(newPermissions == Permissions.MCSM_EXECUTOR) {
			executor.sendMessage(manager, "Error: you cannot set permission level to MCSM executor. "
					+ "This permissions level is reserved for the MCSM process itself.");
			return -1;
		}
		
		changing.setPermissionsLevel(newPermissions);
		
		return 0;
	}
	
}
