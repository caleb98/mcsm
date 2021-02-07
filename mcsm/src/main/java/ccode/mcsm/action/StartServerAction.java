package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.mcserver.event.ServerLoadedEvent;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

/**
 * An action that starts the minecraft server process.
 */
public class StartServerAction extends Action {
	
	public static final String ID = "StartServer";
	
	StartServerAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		MinecraftServer server = manager.getServer();
		
		//Make sure that the server isn't already running
		if(server.isRunning()) {
			executor.sendMessage(manager, "Unable to start server: server already started");
			return -1;
		}

		executor.sendMessage(manager, "Starting server...");
		manager.addListener((event)->{
			if(event instanceof ServerLoadedEvent) {
				executor.sendMessage(manager, "Server ready!");
				return true;
			}
			return false;
		});
		
		//Start the server
		try {
			server.start();
		} catch (IOException e) {
			executor.sendMessage(manager, "Unable to start MinecraftServer process: %s", e.getMessage());
			return -1;
		}
		
		return 0;
	}
	
}
