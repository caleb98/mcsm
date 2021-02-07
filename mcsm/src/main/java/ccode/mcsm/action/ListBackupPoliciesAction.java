package ccode.mcsm.action;

import java.util.HashMap;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.backup.BackupManager;
import ccode.mcsm.backup.BackupPolicy;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

public class ListBackupPoliciesAction extends Action {

	public static final String ID = "ListBackupPolicies";
	
	public ListBackupPoliciesAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		BackupManager bm = manager.getBackupManager();
		HashMap<String, BackupPolicy> policies = bm.getPolicies();
		
		executor.sendMessage(manager, "Current Policies");
		for(String world : policies.keySet()) {
			executor.sendMessage(manager, "> %s: %s", world, policies.get(world).getClass().getSimpleName());
		}

		return 0;
	}
	
}
