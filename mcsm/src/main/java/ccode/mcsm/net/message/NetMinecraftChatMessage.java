package ccode.mcsm.net.message;

public class NetMinecraftChatMessage {

	public final String timestamp;
	public final String playerName;
	public final String message;
	
	@SuppressWarnings("unused")
	private NetMinecraftChatMessage() {
		timestamp = null;
		playerName = null;
		message = null;
	}
	
	public NetMinecraftChatMessage(String timestamp, String playerName, String message) {
		this.timestamp = timestamp;
		this.playerName = playerName;
		this.message = message;
	}
	
}
