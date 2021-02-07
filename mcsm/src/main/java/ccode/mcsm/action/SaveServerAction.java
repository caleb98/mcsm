package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.mcserver.event.WorldSavedEvent;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

/**
 * An action that saves the server via the in game command save-all.
 */
public class SaveServerAction extends Action {

	public static final String ID = "SaveServer";
	
	SaveServerAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		
		//Make sure that the server is running
		if(!manager.getServer().isRunning()) {
			executor.sendMessage(manager, "Unable to save server: server is not running.");
			return -1;
		}
		
		executor.sendMessage(manager, "Saving world...");
		manager.addListener((event)->{
			if(event instanceof WorldSavedEvent) {
				executor.sendMessage(manager, "World saved!");
				return true;
			}
			return false;
		});
		
		try {
			manager.getServer().sendCommand("save-all");
		} catch (IOException e) {
			executor.sendMessage(manager, "Error saving server: %s", e.getMessage());
			return -1;
		}
		
		return 0;
		
	}
	
}
