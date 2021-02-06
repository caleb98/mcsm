package ccode.mcsm.net.message;

public class NetLoginSuccessMessage {

	public final String playerName;
	
	@SuppressWarnings("unused")
	private NetLoginSuccessMessage() {
		playerName = null;
	}
	
	public NetLoginSuccessMessage(String playerName) {
		this.playerName = playerName;
	}
	
}
