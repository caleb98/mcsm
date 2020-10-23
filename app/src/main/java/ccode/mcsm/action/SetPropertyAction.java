package ccode.mcsm.action;

import java.io.IOException;

import ccode.mcsm.MinecraftServerManager;

public class SetPropertyAction extends Action {

	public static final String ID = "SetProperty";
	
	@Override
	public int execute(MinecraftServerManager manager, String args) {
		if(manager.getServer().arePropsLoaded()) {
			
			String[] split = args.split("\\s+");
			if(split.length < 2) {
				System.err.println("Invalid arguments for SetProperty action: " + args);
				return -1;
			}
			
			String prop = split[0];
			String value = args.substring(prop.length()).trim();
			
			try {
				manager.getServer().setProperty(prop, value);
			} catch (IOException e) {
				System.err.printf("Error writing new property to file: %s\n", e.getMessage());
				return -1;
			}
			
			return 0;
		}
		else {
			return -1;
		}
	}
	
}