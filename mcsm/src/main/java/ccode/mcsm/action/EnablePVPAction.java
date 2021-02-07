package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.mcserver.MinecraftServer;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

public class EnablePVPAction extends Action {

	public static final String ID = "EnablePVP";
	
	EnablePVPAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
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
		
		if(pvp.equals("true")){
			executor.sendMessage(manager, "Error: PVP already enabled for this server.");
			return -1;
		}
		
		try {
			server.setProperty("pvp", "true");
		} catch (IOException e) {
			executor.sendMessage(manager, "Error: execption enabling pvp: %s", e.getMessage());
			return -1;
		}
		
		executor.sendMessage(manager, "pvp flag set to true. Server restart required to take effect.");
		return 0;
	}
	
}
