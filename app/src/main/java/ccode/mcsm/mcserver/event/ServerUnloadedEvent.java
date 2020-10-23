package ccode.mcsm.mcserver.event;

import java.util.regex.Pattern;

public class ServerUnloadedEvent extends MinecraftServerEvent {

	public static final Pattern MATCHER = Pattern.compile("\\[(\\d\\d:\\d\\d:\\d\\d)\\] \\[Server thread\\/INFO\\]: ThreadedAnvilChunkStorage \\(DIM1\\): All chunks are saved");
	
	public ServerUnloadedEvent(String timestamp) {
		super(timestamp);
	}
	
}
