package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;

/**
 * An action that sends a specified server command directly
 * to the minecraft server process.
 */
public class SendCommandAction extends Action {

	public static final String ID = "SendCommand";
	
	SendCommandAction() {
		super(ID, Permissions.SERVER_MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, String args) {
		
		//Make sure the server is running
		if(!manager.getServer().isRunning()) {
			System.err.println("Error in SendCommand: server not running");
			return -1;
		}
		
		try {
			manager.getServer().sendCommand(args);
		} catch (IOException e) {
			System.err.printf("Error sending command \'%s\' to server: %s\n", args, e.getMessage());
		}
		
		return 0;
		
	}
	
}
