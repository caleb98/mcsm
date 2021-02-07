package ccode.mcsm.net.message;

public class NetExecutionMessage {

	public final String message;
	
	@SuppressWarnings("unused")
	private NetExecutionMessage() {
		message = null;
	}
	
	public NetExecutionMessage(String message) {
		this.message = message;
	}
	
}
