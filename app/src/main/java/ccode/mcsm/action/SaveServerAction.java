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
		super(ID, Permissions.LEVEL_4);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		
		//Make sure that the server is running
		if(!manager.getServer().isRunning()) {
			System.out.println("Unable to save server: server is not running.");
			return 0;
		}
		
		try {
			manager.getServer().save();
		} catch (IOException e) {
			System.err.printf("Error saving server: %s\n", e.getMessage());
			return -1;
		}
		
		return 0;
		
	}
	
}
