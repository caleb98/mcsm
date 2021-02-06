package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

/**
 * An action that sends a specified server command directly
 * to the minecraft server process.
 */
public class SendCommandAction extends Action {

	public static final String ID = "SendCommand";
	
	SendCommandAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		
		//Make sure the server is running
		if(!manager.getServer().isRunning()) {
			sendMessage(manager, executor, "Error in SendCommand: server not running");
			return -1;
		}
		
		try {
			manager.getServer().sendCommand(args);
		} catch (IOException e) {
			sendMessage(manager, executor, "Error sending command \'%s\' to server: %s", args, e.getMessage());
		}
		
		return 0;
		
	}
	
}