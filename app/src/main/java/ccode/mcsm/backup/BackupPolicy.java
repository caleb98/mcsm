package ccode.mcsm.backup;

import java.io.IOException;

public interface BackupPolicy {
	public boolean needsClean(String worldName);
	public void cleanBackups(String worldName);
	public void backup(String worldName) throws IOException;
}
