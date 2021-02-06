package ccode.mcsm.backup;

public class NoLimitPolicy extends StandardBackupPolicy {
	
	public NoLimitPolicy(BackupManager manager) {
		super(manager);
	}

	@Override
	public boolean needsClean(String backupDir) {
		return false;
	}

	@Override
	public void cleanBackups(String backupDir) {
	
	}
	
}
