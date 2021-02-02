package ccode.mcsm.action;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class SetWorldAction extends Action {

	public static final String ID = "SetWorld";
	
	public static final Pattern ARGUMENT_PATTENR = Pattern.compile("(\\w+)");
	
	public SetWorldAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}

	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		Matcher m = ARGUMENT_PATTENR.matcher(args);
		if(!m.matches()) {
			sendMessage(manager, executor, "Invalid world name.");
			return -1;
		}
		
		String worldName = m.group(1);
		try {
			manager.getServer().setProperty("level-name", worldName);
		} catch (IOException e) {
			sendMessage(manager, executor, "IO error occurred while writing to properites file: %s", e.getMessage());
			return -1;
		}
		
		sendMessage(manager, executor, "World swapped. New world will be loaded on next server launch.");
		return 0;
	}

}
