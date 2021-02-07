package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.mcserver.event.ServerStoppedEvent;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

/**
 * An action that stops the minecraft server process.
 */
public class StopServerAction extends Action {
	
	public static final String ID = "StopServer";
	
	StopServerAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		
		//Make sure the server is running
		if(!manager.getServer().isRunning()) {
			executor.sendMessage(manager, "unable to stop server: server process not active");
			return -1;
		}
		
		executor.sendMessage(manager, "Stopping server...");
		manager.addListener((event)->{
			if(event instanceof ServerStoppedEvent) {
				executor.sendMessage(manager, "Server stopped!");
				return true;
			}
			return false;
		});
		
		try {
			manager.getServer().sendCommand("stop");
		} catch (IOException e) {
			executor.sendMessage(manager, "Error stopping server: %s", e.getMessage());
			return -1;
		}
		
		return 0;
	
	}
	
}
