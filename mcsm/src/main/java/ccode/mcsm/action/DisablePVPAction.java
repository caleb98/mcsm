package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

public class DisablePVPAction extends Action {

	public static final String ID = "DisablePVP";
	
	DisablePVPAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor	executor, String args) {
		//Make sure that the server has loaded properties file correctly.
		MinecraftServer server = manager.getServer();
		if(!server.arePropsLoaded()) {
			executor.sendMessage(manager, "Error: server properties failed to load correctly. Unable to change values.");
			return -1;
		}
		
		//Check value of pvp
		String pvp = server.getProperty("pvp");
		if(pvp == null) {
			executor.sendMessage(manager, "Error: pvp property not found in server.properties.");
			return -1;
		}
		
		if(pvp.equals("false")){
			executor.sendMessage(manager, "Error: PVP already disabled for this server.");
			return -1;
		}
		
		try {
			server.setProperty("pvp", "false");
		} catch (IOException e) {
			executor.sendMessage(manager, "Error: execption disabling pvp: %s", e.getMessage());
			return -1;
		}
		
		executor.sendMessage(manager, "pvp flag set to false. Server restart required to take effect.");
		return 0;
	}
	
}
