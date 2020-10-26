package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

/**
 * An action that stops the minecraft server process.
 */
public class StopServerAction extends Action {
	
	public static final String ID = "StopServer";
	
	StopServerAction() {
		super(ID, Permissions.LEVEL_4);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		
		//Make sure the server is running
		if(!manager.getServer().isRunning()) {
			sendMessage(manager, executor, "unable to stop server: server process not active");
			return 0;
		}
		
		try {
			manager.getServer().stop();
		} catch (IOException e) {
			sendMessage(manager, executor, "Error stopping server: %s", e.getMessage());
			return -1;
		}
		
		return 0;
	
	}
	
}
