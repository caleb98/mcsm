package ccode.mcsm.action;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class SetSeedAction extends Action {

	public static final String ID = "SetSeed";
	
	public static final Pattern ARGUMENT_PATTERN = Pattern.compile("([\\d\\w]+)");
	
	public SetSeedAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		Matcher m = ARGUMENT_PATTERN.matcher(args);
		if(!m.matches()) {
			sendMessage(manager, executor, "Invalid world seed.");
			return -1;
		}
		
		String seed = m.group(1);
		try {
			manager.getServer().setProperty("level-seed", seed);
		} catch (IOException e) {
			sendMessage(manager, executor, "IO error occurred while writing to properites file: %s", e.getMessage());
			return -1;
		}
		
		sendMessage(manager, executor, "Seed set. New worlds will be generated using this seed.");
		return 0;
	}

}
