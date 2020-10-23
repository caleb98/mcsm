package ccode.mcsm.mcserver.event;

import java.util.regex.Pattern;

public class PlayerJoinedEvent extends MinecraftServerEvent {

	public static final String ID = "PlayerJoined";
	public static final Pattern MATCHER = Pattern.compile("\\[(\\d\\d:\\d\\d:\\d\\d)\\] \\[Server thread\\/INFO\\]: (\\w+) joined the game");
	
	public final String playerName;
	
	public PlayerJoinedEvent(String timestamp, String player) {
		super(ID, timestamp);
		playerName = player;
	}
	
}
