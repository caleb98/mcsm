package ccode.mcsm.backup;

import java.io.File;

public class MaxCapacityPolicy extends StandardBackupPolicy {

	private long maxBytes;
	
	public MaxCapacityPolicy(BackupManager manager, long maxBytes) {
		super(manager);
		this.maxBytes = maxBytes;
	}
	
	public long getMaxBytes() {
		return maxBytes;
	}
	
	public void setMaxBytes(long maxBytes) {
		this.maxBytes = maxBytes;
	}
	
	@Override
	public boolean needsClean(String worldName) {
		File[] backups = manager.getBackupFiles(worldName);
		long size = 0;
		for(File backup : backups) {
			size += backup.length();
		}
		
		/*
		 * backups.length > 1 is important here.
		 * If the world size exceeds our max bytes value,
		 * then not having this check would mean that the 
		 * backup policy would clean even if only one backup
		 * is available.
		 * 
		 * By adding this check, we make sure only to clean
		 * if there is greater than one backup, avoiding
		 * the problem of deleting all backups when world
		 * size exceeds maxBytes.
		 */
		return size > maxBytes && backups.length > 1;
	}
	
}
