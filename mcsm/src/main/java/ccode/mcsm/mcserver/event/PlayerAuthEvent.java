package ccode.mcsm.mcserver.event;

import java.util.regex.Pattern;

public class PlayerAuthEvent extends MinecraftServerEvent {

	public static final String ID = "PlayerAuth";
	public static final Pattern MATHCHER = Pattern.compile(
			"^\\[(\\d{2}:\\d{2}:\\d{2})\\] \\[User Authenticator #\\d\\/INFO]: UUID of player (\\w+) is ([\\dabcdef]{8}-[\\dabcdef]{4}-[\\dabcdef]{4}-[\\dabcdef]{4}-[\\dabcdef]{12})");

	public final String player;
	public final String uuid;

	public PlayerAuthEvent(String timestamp, String player, String uuid) {
		super(ID, timestamp);
		this.player = player;
		this.uuid = uuid;
	}

}
