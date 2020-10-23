package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;

public class SendCommandAction extends Action {

	public static final String ID = "SendCommand";
	
	@Override
	public int execute(MinecraftServerManager manager, String args) {
		
		//Make sure the server is running
		if(!manager.getServer().isRunning()) {
			System.out.println("Unable to send command: server not running");
			return 0;
		}
		
		try {
			manager.getServer().sendCommand(args);
		} catch (IOException e) {
			System.err.printf("Error sending command \'%s\' to server: %s\n", args, e.getMessage());
		}
		
		return 0;
		
	}
	
}
