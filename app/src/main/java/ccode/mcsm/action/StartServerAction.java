package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.permissions.Permissions;

/**
 * An action that starts the minecraft server process.
 */
public class StartServerAction extends Action {
	
	public static final String ID = "StartServer";
	
	StartServerAction() {
		super(ID, Permissions.LEVEL_4);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, String args) {
		MinecraftServer server = manager.getServer();
		
		//Make sure that the server isn't already running
		if(server.isRunning()) {
			System.out.println("Unable to start server: server already started");
			return 0;
		}
		
		//Start the server
		try {
			server.start();
		} catch (IOException e) {
			System.err.printf("Unable to start MinecraftServer process: %s\n", e.getMessage());
			return -1;
		}
		
		return 0;
	}
	
}
