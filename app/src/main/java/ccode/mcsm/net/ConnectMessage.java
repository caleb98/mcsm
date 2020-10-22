package ccode.mcsm.net;

public class ConnectMessage {

	public final String username;
	public final String password;

	private ConnectMessage() {
		username = null;
		password = null;
	}
	
	public ConnectMessage(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
}
