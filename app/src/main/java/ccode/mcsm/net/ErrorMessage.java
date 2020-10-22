package ccode.mcsm.net;

public class ErrorMessage {

	public final String error;
	
	@SuppressWarnings("unused")
	private ErrorMessage() {
		error = null;
	}
	
	public ErrorMessage(String err) {
		error = err;
	}
	
}
