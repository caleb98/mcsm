package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class EnablePVPAction extends Action {

	public static final String ID = "EnablePVP";
	
	EnablePVPAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		//Make sure that the server has loaded properties file correctly.
		MinecraftServer server = manager.getServer();
		if(!server.arePropsLoaded()) {
			sendMessage(manager, executor, "Error: server properties failed to load correctly. Unable to change values.");
			return -1;
		}
		
		//Check value of pvp
		String pvp = server.getProperty("pvp");
		if(pvp == null) {
			sendMessage(manager, executor, "Error: pvp property not found in server.properties.");
			return -1;
		}
		
		if(pvp.equals("true")){
			sendMessage(manager, executor, "Error: PVP already enabled for this server.");
			return -1;
		}
		
		try {
			server.setProperty("pvp", "true");
		} catch (IOException e) {
			sendMessage(manager, executor, "Error: execption enabling pvp: %s", e.getMessage());
			return -1;
		}
		
		sendMessage(manager, executor, "pvp flag set to true. Server restart required to take effect.");
		return 0;
	}
	
}
