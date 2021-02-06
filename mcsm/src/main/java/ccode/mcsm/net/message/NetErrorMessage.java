package ccode.mcsm.net.message;

public class NetErrorMessage {

	public final String topic;
	public final String message;
	
	@SuppressWarnings("unused")
	private NetErrorMessage() {
		topic = null;
		message = null;
	}
	
	public NetErrorMessage(String topic, String message) {
		this.topic = topic;
		this.message = message;
	}
	
}
