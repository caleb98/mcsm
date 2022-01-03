package ccode.mcsm.action;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.backup.BackupManager;
import ccode.mcsm.backup.BackupPolicy;
import ccode.mcsm.backup.MaxCapacityPolicy;
import ccode.mcsm.backup.MaxCountPolicy;
import ccode.mcsm.backup.NoLimitPolicy;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

public class BackupPolicyInfoAction extends Action {

	public static final String ID = "BackupPolicyInfo";
	
	public BackupPolicyInfoAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}

	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		String worldName = args.trim();
		
		if(worldName.equals("")) {
			executor.sendMessage(manager, "No world name provided. Please provide the world you would like to retrieve backup policy information for.");
			return -1;
		}
		
		BackupManager bm = manager.getBackupManager();
		BackupPolicy policy = bm.getPolicies().get(worldName);
		
		if(policy == null) {
			executor.sendMessage(manager, "That world has no specified backup policies.");
			return -1;
		}
		
		executor.sendMessage(manager, "Backup Policy for %s:", worldName);
		
		if(policy instanceof NoLimitPolicy) {
			executor.sendMessage(manager, "Type: NoLimitPolicy");
		}
		else if(policy instanceof MaxCountPolicy) {
			MaxCountPolicy maxCount = (MaxCountPolicy) policy;
			executor.sendMessage(manager, "Type: MaxCountPolicy");
			executor.sendMessage(manager, "MaxCount: %d backups", maxCount.getMaxBackups());
		}
		else if(policy instanceof MaxCapacityPolicy) {
			MaxCapacityPolicy maxCap = (MaxCapacityPolicy) policy;
			executor.sendMessage(manager, "Type: MaxCapacityPolicy");
			executor.sendMessage(manager, "MaxCapaciy: %d bytes", maxCap.getMaxBytes());
		}
		else {
			executor.sendMessage(manager, "Unrecognized backup policy.");
		}
		
		return 0;
	}
	
}
