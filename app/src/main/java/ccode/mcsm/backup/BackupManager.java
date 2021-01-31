package ccode.mcsm.backup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {

	private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd_HH.mm.ss");
	
	/**
	 * The directory of the server that we will do backups for.
	 */
	private transient File serverDir;
	
	/**
	 * The directory where the backups are stored.
	 */
	private String backupDirectory;
	
	/**
	 * The maximum number of backups per world.
	 */
	private int maxWorldBackups;
	
	public BackupManager(File serverDir, String backupDirectory, int maxWorldBackups) {
		this.serverDir = serverDir;
		this.backupDirectory = backupDirectory;
		this.maxWorldBackups = maxWorldBackups;
	}
	
	public void setServerDir(File serverDir) {
		this.serverDir = serverDir;
	}
	
	public File getServerDir() {
		return serverDir;
	}
	
	public void setMaxWorldBackups(int max) {
		maxWorldBackups = max;
	}
	
	public int getMaxWorldBackups() {
		return maxWorldBackups;
	}
	
	public void setBackupDirectory(String backupDir) {
		backupDirectory = backupDir;
	}
	
	public String getBackupDirectory() {
		return backupDirectory;
	}
	
	public void backup(String worldName) throws IOException {
		
		File worldDir = new File(serverDir.getPath() + File.separator + worldName);
		if(!worldDir.exists() || !worldDir.isDirectory()) {
			throw new IllegalArgumentException("Provided world file is not valid.");
		}
		
		String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
		File outputDir = new File(backupDirectory + File.separator + worldName);
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
			 * can be turned back on using:
			 * 
			 * 		save-on
			 */
			File[] toZip = worldDir.listFiles((f)->{return !f.getName().equals("session.lock");});
			for(File f : toZip) {
				addFiles(worldName, f, zos);
			}
			zos.close();
			
		}
		
		checkBackupCount(worldName);
		
	}
	
	private void checkBackupCount(String worldName) {
		File worldBackupDir = new File(backupDirectory + File.separator + worldName);
		
		//Get backup files
		File[] backups = worldBackupDir.listFiles((f)->{
			return f.getName().matches("\\d{4}\\.\\d{2}\\.\\d{2}_\\d{2}\\.\\d{2}\\.\\d{2}\\.zip");
		});
		
		//If we have more backups than allowed, delete oldest.
		if(backups.length > maxWorldBackups) {
			LocalDateTime oldest = LocalDateTime.now();
			
			for(File backup : backups) {
				String timestamp = backup.getName().replace(".zip", "");
				LocalDateTime backupTime = TIMESTAMP_FORMATTER.parse(timestamp, LocalDateTime::from);
				if(backupTime.isBefore(oldest)) {
					oldest = backupTime;
				}
			}
			
			String oldestFileName = TIMESTAMP_FORMATTER.format(oldest) + ".zip";
			File oldestBackup = new File(backupDirectory + File.separator + worldName + File.separator + oldestFileName);
			oldestBackup.delete();
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
