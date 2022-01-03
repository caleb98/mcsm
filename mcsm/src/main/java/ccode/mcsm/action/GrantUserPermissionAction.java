package ccode.mcsm.action;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class GrantUserPermissionAction extends Action {

	public static final String ID = "GrantUserPermission";
	
	public GrantUserPermissionAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		String[] argsArr = args.trim().split("\\s+");
		
		if(argsArr.length != 2) {
			executor.sendMessage(manager, "Inavlid number of arguments. Provide the player name and the action to override.");
			return -1;
		}
		
		String playerName = argsArr[0];
		String action = argsArr[1];
		
		Player player = manager.getPlayerFromName(playerName);
		if(player == null) {
			executor.sendMessage(manager, "Invalid player.");
			return -1;
		}
		
		player.addPermission(action);
		executor.sendMessage(manager, "Permission override added.");
		
		return 0;
	}
	
}
