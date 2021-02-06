package ccode.mcsm.mcserver.event;

public class ServerStoppedEvent extends MinecraftServerEvent {

	public static final String ID = "ServerStopped";
	
	public ServerStoppedEvent(String timestamp) {
		super(ID, timestamp);
	}
	
}
