package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;

public class StopServerAction extends Action {

	public static final String ID = "StopServer";
	
	@Override
	public int execute(MinecraftServerManager manager, String args) {
		
		//Make sure the server is running
		if(!manager.getServer().isRunning()) {
			System.out.println("unable to stop server: server process not active");
			return 0;
		}
		
		try {
			manager.getServer().stop();
		} catch (IOException e) {
			System.err.printf("Error stopping server: %s\n", e.getMessage());
			return -1;
		}
		
		return 0;
	
	}
	
}
