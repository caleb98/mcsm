package ccode.mcsm.net.message;

public class NetDoActionMessage {

	public final String actionId;
	public final String arguments;
	
	@SuppressWarnings("unused")
	private NetDoActionMessage() {
		actionId = null;
		arguments = null;
	}
	
	public NetDoActionMessage(String actionID, String arguments) {
		this.actionId = actionID;
		this.arguments = arguments;
	}
	
}
