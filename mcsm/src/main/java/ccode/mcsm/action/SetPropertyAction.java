package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

/**
 * An action that sets a specific property in the server.properties
 * file and immediately saves that property change.
 */
public class SetPropertyAction extends Action {

	public static final String ID = "SetProperty";
	
	SetPropertyAction() {
		super(ID, Permissions.SERVER_OWNER);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		if(manager.getServer().arePropsLoaded()) {
			
			String[] split = args.split("\\s+");
			if(split.length < 2) {
				executor.sendMessage(manager, "Invalid arguments for SetProperty action: %s", args);
				return -1;
			}
			
			String prop = split[0];
			String value = args.substring(prop.length()).trim();
			
			try {
				manager.getServer().setProperty(prop, value);
			} catch (IOException e) {
				executor.sendMessage(manager, "Error writing new property to file: %s", e.getMessage());
				return -1;
			}
			
			return 0;
		}
		else {
			return -1;
		}
	}
	
}
