package ccode.mcsm.net.message;

public class ActionMessage {

	public final String action;
	
	private ActionMessage() {
		action = null;
	}
	
	public ActionMessage(String action) {
		this.action = action;
	}
	
}
