package ccode.mcsm.mcserver.event;

import java.util.regex.Pattern;

public class PlayerLeftEvent extends MinecraftServerEvent {

	public static final Pattern MATCHER = Pattern.compile("\\[(\\d\\d:\\d\\d:\\d\\d)\\] \\[Server thread\\/INFO\\]: (\\w+) left the game");
	
	public final String playerName;
	
	public PlayerLeftEvent(String timestamp, String player) {
		super(timestamp);
		playerName = player;
	}
	
}
