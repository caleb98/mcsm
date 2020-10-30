package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class DisablePVPAction extends Action {

	public static final String ID = "DisablePVP";
	
	DisablePVPAction() {
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
		
		if(pvp.equals("false")){
			sendMessage(manager, executor, "Error: PVP already disabled for this server.");
			return -1;
		}
		
		try {
			server.setProperty("pvp", "false");
		} catch (IOException e) {
			sendMessage(manager, executor, "Error: execption disabling pvp: %s", e.getMessage());
			return -1;
		}
		
		sendMessage(manager, executor, "pvp flag set to false. Server restart required to take effect.");
		return 0;
	}
	
}
