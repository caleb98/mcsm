package ccode.mcsm.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class StandardBackupPolicy implements BackupPolicy {

	protected BackupManager manager;
	
	public StandardBackupPolicy(BackupManager manager) {
		this.manager = manager;
	}
	
	public void setBackupManager(BackupManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void cleanBackups(String worldName) {
		while(needsClean(worldName)) {
			
			//Find oldest backup
			LocalDateTime oldest = LocalDateTime.now();
			for(File backup : manager.getBackupFiles(worldName)) {
				String timestamp = backup.getName().replace(".zip", "");
				LocalDateTime backupTime = BackupManager.TIMESTAMP_FORMATTER.parse(timestamp, LocalDateTime::from);
				if(backupTime.isBefore(oldest)) {
					oldest = backupTime;
				}
			}
			
			//Delete
			String oldestFileName = BackupManager.TIMESTAMP_FORMATTER.format(oldest) + ".zip";
			File oldestBackup = new File(manager.getBackupDirectory() + File.separator + worldName + File.separator + oldestFileName);
			oldestBackup.delete();
			
		}
	}
	
	@Override
	public void backup(String worldName) throws IOException {
		File worldDir = new File(manager.getServerDir().getPath() + File.separator + worldName);
		if(!worldDir.exists() || !worldDir.isDirectory()) {
			throw new IllegalArgumentException("Provided world file is not valid.");
		}
		
		String timestamp = LocalDateTime.now().format(BackupManager.TIMESTAMP_FORMATTER);
		File outputDir = new File(manager.getBackupDirectory() + File.separator + worldName);
		File zipFile = new File(outputDir.getPath() + File.separator + timestamp + ".zip");
		
		//Create the output directory if it doesn't exist
		if(!outputDir.exists()) {
			outputDir.mkdirs();
		}
		
		try(
				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))
		) {
			
			/*
			 * We ignore session.lock here because it is the only file that the minecraft
			 * server process locks while running. While it can be unsafe to copy a minecraft
			 * world while the server is running, it is only unsafe if the server is actively
			 * saving the world file. Thus, running the following commands:
			 * 
			 * 		save-off
			 * 		save-all
			 * 
			 * Ensures that the server is no longer saving and that the most recent world
			 * state has been written to the disk. After the backup is complete, auto saving
			 * can be turned back on using:6
			 * 
			 * 		save-on
			 */
			File[] toZip = worldDir.listFiles((f)->{return !f.getName().equals("session.lock");});
			for(File f : toZip) {
				addFiles(worldName, f, zos);
			}
			zos.close();
			
		}
	}
	
	private void addFiles(String parentName, File file, ZipOutputStream zos) throws IOException {
		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				addFiles(parentName + File.separator + file.getName(), f, zos);
			}
		}
		else {
			zos.putNextEntry(new ZipEntry(parentName + File.separator + file.getName()));
			byte[] fBytes = Files.readAllBytes(file.toPath());
			zos.write(fBytes);
			zos.closeEntry();
		}
	}
	
}
