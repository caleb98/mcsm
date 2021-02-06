package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

/**
 * An action that starts the minecraft server process.
 */
public class StartServerAction extends Action {
	
	public static final String ID = "StartServer";
	
	StartServerAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		MinecraftServer server = manager.getServer();
		
		//Make sure that the server isn't already running
		if(server.isRunning()) {
			sendMessage(manager, executor, "Unable to start server: server already started");
			return -1;
		}
		
		//Start the server
		try {
			server.start();
		} catch (IOException e) {
			sendMessage(manager, executor, "Unable to start MinecraftServer process: %s", e.getMessage());
			return -1;
		}
		
		return 0;
	}
	
}
