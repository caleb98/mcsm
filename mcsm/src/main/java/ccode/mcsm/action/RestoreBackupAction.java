package ccode.mcsm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.mcserver.event.BackupRestoredEvent;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

public class RestoreBackupAction extends Action {

	public static final String ID = "RestoreBackup";
	
	public static final Pattern ARGUMENTS_PATTERN = Pattern.compile("(\\w+) (\\d+)");
	
	public RestoreBackupAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		if(manager.getServer().isRunning()) {
			executor.sendMessage(manager, "Error: cannot restore backup while server is running.");
			return -1;
		}

		Matcher m = ARGUMENTS_PATTERN.matcher(args);
		if(!m.matches()) {
			executor.sendMessage(manager, "Invalid arguments, please provide world name and backup number to restore.");
			return -1;
		}
		
		String worldName = m.group(1);
		int backupNumber = Integer.parseInt(m.group(2)) - 1;
		
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
		
		File currentWorldFile = null;
		for(File world : worldDirs) {
			if(world.getName().equals(worldName)) {
				currentWorldFile = world;
				break;
			}
		}
		
		if(currentWorldFile == null) {
			executor.sendMessage(manager, "That world does not exist.");
			return -1;
		}
		
		//Get backup file.
		File[] backups = manager.getBackupManager().getBackupFiles(worldName);
		if(backupNumber >= backups.length) {
			executor.sendMessage(manager, "Invalid backup number.");
			return -1;
		}
		File restore = backups[backupNumber];
		
		//Rename current file in case restore fails
		File tempCurrentWorld;
		try {
			tempCurrentWorld = new File(currentWorldFile.getCanonicalPath() + "_temp");
		} catch (IOException e1) {
			System.err.printf("Error renaming existing world folder: %s\n", e1.getMessage());
			return -1;
		}
		if(tempCurrentWorld.exists()) {
			deleteRecursive(tempCurrentWorld);
		}
		if(!currentWorldFile.renameTo(tempCurrentWorld)) {
			executor.sendMessage(manager, "Unable to copy existing world file.");
			return -1;
		}
		
		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(restore));
			ZipEntry zipEntry = zis.getNextEntry();
			byte[] buffer = new byte[1024];
			
			while(zipEntry != null) {
				File newFile = newFile(manager.getServerDirectory(), zipEntry);
				if(zipEntry.isDirectory()) {
					if(!newFile.isDirectory() && !newFile.mkdirs()) {
						zis.close();
						throw new IOException("Failed to create directory " + newFile);
					}
				}
				else {
					File parent = newFile.getParentFile();
					if(!parent.isDirectory() && !parent.mkdirs()) {
						zis.close();
						throw new IOException("Failed to create directory " + parent);
					}
					
					FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
				}
				
				zipEntry = zis.getNextEntry();
			}
			
			zis.closeEntry();
			zis.close();
		} catch (FileNotFoundException fnfe) {
			executor.sendMessage(manager, "Backup file not found.");
			tempCurrentWorld.renameTo(currentWorldFile);
			return -1;
		} catch (IOException e) {
			executor.sendMessage(manager, "Error reading backup file: %s", e.getMessage());
			currentWorldFile.delete();
			tempCurrentWorld.renameTo(currentWorldFile);
			return -1;
		}
		
		deleteRecursive(tempCurrentWorld);
		manager.addEvent(new BackupRestoredEvent(DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now())));
		executor.sendMessage(manager, "Backup successfully restored.");
		return 0;
	}
	
	private File newFile(File destDir, ZipEntry entry) throws IOException {
		File destFile = new File(destDir, entry.getName());
		
		String destDirPath = destDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();
		
		if(!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target directory: " + entry.getName());
		}
		
		return destFile;
	}
	
	/**
	 * Deletes a directory recursively, including all sub-directories and files.
	 * @param directory
	 */
	private void deleteRecursive(File directory) {
		for(File f : directory.listFiles()) {
			if(f.isDirectory()) {
				deleteRecursive(f);
			}
			else {
				f.delete();
			}
		}
		directory.delete();
	}
	
}
