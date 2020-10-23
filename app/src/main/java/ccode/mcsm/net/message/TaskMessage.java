package ccode.mcsm.net.message;

public class TaskMessage {

	public final String task;
	
	@SuppressWarnings("unused")
	private TaskMessage() {
		task = null;
	}
	
	public TaskMessage(String task) {
		this.task = task;
	}
	
}
