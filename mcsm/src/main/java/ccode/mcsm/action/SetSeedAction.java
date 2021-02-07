package ccode.mcsm.action;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

public class SetSeedAction extends Action {

	public static final String ID = "SetSeed";
	
	public static final Pattern ARGUMENT_PATTERN = Pattern.compile("([\\d\\w]+)");
	
	public SetSeedAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		Matcher m = ARGUMENT_PATTERN.matcher(args);
		if(!m.matches()) {
			executor.sendMessage(manager, "Invalid world seed.");
			return -1;
		}
		
		String seed = m.group(1);
		try {
			manager.getServer().setProperty("level-seed", seed);
		} catch (IOException e) {
			executor.sendMessage(manager, "IO error occurred while writing to properites file: %s", e.getMessage());
			return -1;
		}
		
		executor.sendMessage(manager, "Seed set. New worlds will be generated using this seed.");
		return 0;
	}

}
