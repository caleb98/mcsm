package ccode.mcsm.mcserver.event;

import java.util.regex.Pattern;

public class AchievementGetEvent extends MinecraftServerEvent {

	public static final Pattern MATCHER = Pattern.compile("\\[(\\d\\d:\\d\\d:\\d\\d)\\] \\[Server thread\\/INFO\\]: (\\w+) has made the advancement \\[(.*)\\]");
	
	public final String playerName;
	public final String achievement;
	
	public AchievementGetEvent(String timestamp, String player, String achievement) {
		super(timestamp);
		playerName = player;
		this.achievement = achievement;
	}
	
}
