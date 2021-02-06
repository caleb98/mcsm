package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

/**
 * An action that saves the server via the in game command save-all.
 */
public class SaveServerAction extends Action {

	public static final String ID = "SaveServer";
	
	SaveServerAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		
		//Make sure that the server is running
		if(!manager.getServer().isRunning()) {
			sendMessage(manager, executor, "Unable to save server: server is not running.");
			return -1;
		}
		
		try {
			manager.getServer().sendCommand("save-all");
		} catch (IOException e) {
			sendMessage(manager, executor, "Error saving server: %s", e.getMessage());
			return -1;
		}
		
		return 0;
		
	}
	
}
