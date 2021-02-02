package ccode.mcsm.action;

import java.io.File;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class ListWorldsAction extends Action {

	public static final String ID = "ListWorlds";
	
	public ListWorldsAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		File serverDir = manager.getServerDirectory();
		File[] directories = serverDir.listFiles((f)->{
			if(f.isDirectory()) {
				for(File inside : f.listFiles()) {
					if(inside.getName().equals("level.dat")) {
						return true;
					}
				}
			}
			return false;
		});
		
		sendMessage(manager, executor, "Existing Worlds:");
		for(File worldDir : directories) {
			sendMessage(manager, executor, "> %s", worldDir.getName());
		}
		
		return 0;
	}

}
