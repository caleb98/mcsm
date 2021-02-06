package ccode.mcsm.action;

import java.util.HashMap;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.backup.BackupManager;
import ccode.mcsm.backup.BackupPolicy;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class ListBackupPoliciesAction extends Action {

	public static final String ID = "ListBackupPolicies";
	
	public ListBackupPoliciesAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		BackupManager bm = manager.getBackupManager();
		HashMap<String, BackupPolicy> policies = bm.getPolicies();
		
		sendMessage(manager, executor, "Current Policies");
		for(String world : policies.keySet()) {
			sendMessage(manager, executor, "> %s: %s", world, policies.get(world).getClass().getSimpleName());
		}

		return 0;
	}
	
}
