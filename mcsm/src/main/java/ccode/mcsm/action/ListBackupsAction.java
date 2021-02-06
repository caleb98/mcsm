package ccode.mcsm.action;

import java.io.File;
import java.time.LocalDateTime;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.backup.BackupManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class ListBackupsAction extends Action {
	
	public static final String ID = "ListBackups";
	
	public ListBackupsAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		
		if(args.matches("\\s*")) {
			sendMessage(manager, executor, "Please provide a world name.");
			return -1;
		}
		
		//Make sure that world exists.
		File[] worldDirs = manager.getServerDirectory().listFiles((f)->{
			if(f.isDirectory()) {
				for(File sub : f.listFiles()) {
					if(sub.getName().equals("level.dat")) {
						return true;
					}
				}
			}
			return false;
		});
		
		boolean worldExists = false;
		for(File world : worldDirs) {
			if(world.getName().equals(args)) {
				worldExists = true;
				break;
			}
		}
		
		if(!worldExists) {
			sendMessage(manager, executor, "That world does not exist.");
			return 0;
		}
		
		File[] backups = manager.getBackupManager().getBackupFiles(args);
		
		if(backups.length == 0) {
			sendMessage(manager, executor, "There are no backups for this world.");
			return 0;
		}
		
		sendMessage(manager, executor, "Available backups for \"%s\"", args);
		int backupNum = 1;
		for(File f : backups) {
			String backupName = f.getName().replace(".zip", "");
			LocalDateTime backupTime = BackupManager.TIMESTAMP_FORMATTER.parse(backupName, LocalDateTime::from);
			sendMessage(manager, executor, "%d > %s", backupNum++, backupTime);
		}
		
		return 0;
	}

}
