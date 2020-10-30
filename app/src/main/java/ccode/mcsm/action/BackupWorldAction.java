package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class BackupWorldAction extends Action {

	public static final String ID = "BackupWorld";
	
	BackupWorldAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {		
		if(args.length() == 0) {
			sendMessage(manager, executor, "Error with backup: no world name provided");
			return -1;
		}
		
		try {
			manager.getBackupManager().backup(args);
		} catch (IOException e) {
			sendMessage(manager, executor, "Error while creating backup: %s", e.getMessage());
			return -1;
		}
		
		return 0;
	}
	
}
