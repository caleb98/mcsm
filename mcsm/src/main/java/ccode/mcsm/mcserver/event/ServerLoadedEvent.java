package ccode.mcsm.mcserver.event;

import java.util.regex.Pattern;

public class ServerLoadedEvent extends MinecraftServerEvent {

	public static final String ID = "ServerLoaded";
	public static final Pattern MATCHER = Pattern.compile("\\[(\\d\\d:\\d\\d:\\d\\d)\\] \\[Server thread\\/INFO\\]: Done \\((\\d+\\.\\d+)s\\)! For help, type \"help\"");
	
	public final String startupTime;
	
	public ServerLoadedEvent(String timestamp, String startupTime) {
		super(ID, timestamp);
		this.startupTime = startupTime;
	}
	
}
