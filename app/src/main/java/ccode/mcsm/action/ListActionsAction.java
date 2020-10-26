package ccode.mcsm.action;

import java.util.ArrayList;
import java.util.Collections;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

/**
 * A utility action that prints each of the available actions
 * to the console.
 */
public class ListActionsAction extends Action {
	
	public static final String ID = "ListActions";
	
	ListActionsAction() {
		super(ID, Permissions.LEVEL_0);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		ArrayList<String> actions = new ArrayList<>(Action.getActions());
		Collections.sort(actions);
		
		sendMessage(manager, executor, "Available actions:");
		for(String action : actions) {
			if(executor.hasPermissions(action))
				sendMessage(manager, executor, " > %s", action);
		}
		
		return 0;
	}
	
}
