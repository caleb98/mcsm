package ccode.mcsm.mcserver.event;

public class BackupRestoredEvent extends MinecraftServerEvent {

	public static final String ID = "BackupRestored";
	
	public BackupRestoredEvent(String timestamp) {
		super(ID, timestamp);
	}
	
}
