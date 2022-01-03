package ccode.mcsm.action;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class RevokeUserPermissionAction extends Action {

	public static final String ID = "RevokeUserPermission";
	
	public RevokeUserPermissionAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		String[] argsArr = args.trim().split("\\s+");
		
		if(argsArr.length != 2) {
			executor.sendMessage(manager, "Inavlid number of arguments. Provide the player name and the action permission to revoke.");
			return -1;
		}
		
		String playerName = argsArr[0];
		String action = argsArr[1];
		
		Player player = manager.getPlayerFromName(playerName);
		if(player == null) {
			executor.sendMessage(manager, "Invalid player.");
			return -1;
		}
		
		player.removePermission(action);
		executor.sendMessage(manager, "Permission revoked.");
		
		return 0;
	}
	
}