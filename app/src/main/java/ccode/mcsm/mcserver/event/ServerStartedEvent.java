package ccode.mcsm.mcserver.event;

public class ServerStartedEvent extends MinecraftServerEvent {
	
	public static final String ID = "ServerStarted";
	
	public ServerStartedEvent(String timestamp) {
		super(ID, timestamp);
	}
	
}
