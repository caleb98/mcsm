package ccode.mcsm.net.message;

public class NetLoginMessage {
	
	public final String playerName;
	public final String password;
	
	@SuppressWarnings("unused")
	private NetLoginMessage() {
		playerName = null;
		password = null;
	}
	
	public NetLoginMessage(String playerName, String password) {
		this.playerName = playerName;
		this.password = password;
	}

}
