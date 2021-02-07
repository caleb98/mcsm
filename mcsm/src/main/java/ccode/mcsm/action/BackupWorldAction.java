package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

public class BackupWorldAction extends Action {

	public static final String ID = "BackupWorld";
	
	BackupWorldAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {		
		if(args.length() == 0) {
			executor.sendMessage(manager, "Error with backup: no world name provided");
			return -1;
		}
		
		try {
			manager.getBackupManager().backup(args);
		} catch (IOException e) {
			executor.sendMessage(manager, "Error while creating backup: %s", e.getMessage());
			return -1;
		}
		
		executor.sendMessage(manager, "Backup successful.");
		
		return 0;
	}
	
}
