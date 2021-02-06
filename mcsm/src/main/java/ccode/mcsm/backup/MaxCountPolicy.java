package ccode.mcsm.backup;

public class MaxCountPolicy extends StandardBackupPolicy {
	
	private int maxBackups;
	
	public MaxCountPolicy(BackupManager manager, int maxBackups) {
		super(manager);
		this.maxBackups = maxBackups;
	}
	
	public int getMaxBackups() {
		return maxBackups;
	}
	
	public void setMaxBackups(int maxBackups) {
		this.maxBackups = maxBackups;
	}
	
	@Override
	public boolean needsClean(String worldName) {
		return manager.getBackupFiles(worldName).length > maxBackups;
	}
	
}
