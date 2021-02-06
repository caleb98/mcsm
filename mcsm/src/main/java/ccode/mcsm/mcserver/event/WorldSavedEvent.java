package ccode.mcsm.mcserver.event;

import java.util.regex.Pattern;

public class WorldSavedEvent extends MinecraftServerEvent {
	
	public static final String ID = "WorldSaved";
	public static final Pattern MATCHER = Pattern.compile("\\[(\\d{2}:\\d{2}:\\d{2})\\] \\[Server thread\\/INFO\\]: Saved the game");
	
	public WorldSavedEvent(String timestamp) {
		super(ID, timestamp);
	}
	
}
