package ccode.mcsm.action;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class SetPermissionLevelAction extends Action {

	public static final String ID = "SetPermissionLevel";
	
	SetPermissionLevelAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}

	@Override
	public int execute(MinecraftServerManager manager, String args) {
		String[] split = args.split("\s+");
		if(split.length != 2) {
			System.err.println("Error: invalid number of arguments for SetPermissionsLevel");
			return -1;
		}
		
		Player p = manager.getPlayerFromName(split[0]);
		if(p == null) {
			System.err.println("Error: invalid player name");
			return -1;
		}
		
		Permissions newPermissions;
		try {
			newPermissions = Permissions.valueOf(split[1]);
		} catch (IllegalArgumentException e) {
			System.err.println("Error: invalid permissions string");
			return -1;
		}
		
		p.setPermissionsLevel(newPermissions);
		
		//TODO: this needs a lot more logic regarding who can change who's permissions
		//if(this change is allowed) { ... }
		
		return 0;
	}
	
}
