package ccode.mcsm.net.message;

public class NetLoginMessage {
	
	public final String playerName;
	public final String passwordHash;
	
	@SuppressWarnings("unused")
	private NetLoginMessage() {
		playerName = null;
		passwordHash = null;
	}
	
	public NetLoginMessage(String playerName, String passwordHash) {
		this.playerName = playerName;
		this.passwordHash = passwordHash;
	}

}
