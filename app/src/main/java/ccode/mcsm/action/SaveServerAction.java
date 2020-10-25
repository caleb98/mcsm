package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;

/**
 * An action that saves the server via the in game command save-all.
 */
public class SaveServerAction extends Action {

	public static final String ID = "SaveServer";
	
	SaveServerAction() {
		super(ID, 4);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, String args) {
		
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
