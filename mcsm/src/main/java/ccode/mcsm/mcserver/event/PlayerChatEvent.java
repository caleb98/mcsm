package ccode.mcsm.mcserver.event;

import java.util.regex.Pattern;

public class PlayerChatEvent extends MinecraftServerEvent {

	public static final String ID = "PlayerChat";
	public static final Pattern MATCHER = Pattern.compile(
			"^\\[(\\d{2}:\\d{2}:\\d{2})\\] \\[Server thread\\/INFO\\]: <(\\w+)> (.*)");
	
	public final String player;
	public final String message;
	
	public PlayerChatEvent(String timestamp, String player, String message) {
		super(ID, timestamp);
		this.player = player;
		this.message = message;
	}
	
}
