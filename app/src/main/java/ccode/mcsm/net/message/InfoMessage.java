package ccode.mcsm.net.message;

public class InfoMessage {

	public final String info;
	
	private InfoMessage() {
		info = null;
	}
	
	public InfoMessage(String info) {
		this.info = info;
	}
	
}