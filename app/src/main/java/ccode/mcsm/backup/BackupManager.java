package ccode.mcsm.backup;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class BackupManager {
	
	public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss");
	
	/**
	 * The directory of the server that we will do backups for.
	 */
	private transient File serverDir;
	
	/**
	 * The directory where the backups are stored.
	 */
	private String backupDirectory;
	
	//worldName -> BackupPolicy
	private HashMap<String, BackupPolicy> backupPolicies = new HashMap<>();
	
	public BackupManager(File serverDir, String backupDirectory, int maxWorldBackups) {
		this.serverDir = serverDir;
		this.backupDirectory = backupDirectory;
	}
	
	public void setServerDir(File serverDir) {
		this.serverDir = serverDir;
	}
	
	public File getServerDir() {
		return serverDir;
	}
	
	public void setBackupDirectory(String backupDir) {
		backupDirectory = backupDir;
	}
	
	public String getBackupDirectory() {
		return backupDirectory;
	}
	
	public void backup(String worldName) throws IOException {
		//Use NoLimitPolicy if no policy is set
		if(backupPolicies.get(worldName) == null) {
			backupPolicies.put(worldName, new NoLimitPolicy(this));
		}
		
		//Get backup policy and backup
		BackupPolicy policy = backupPolicies.get(worldName);
		policy.backup(worldName);
		
		//Check for clean
		if(policy.needsClean(worldName)) {
			policy.cleanBackups(worldName);
		}
	}
	
	public HashMap<String, BackupPolicy> getPolicies() {
		return backupPolicies;
	}
	
}
