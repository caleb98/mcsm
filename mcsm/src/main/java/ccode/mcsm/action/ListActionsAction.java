package ccode.mcsm.action;

import java.util.ArrayList;
import java.util.Collections;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

/**
 * A utility action that prints each of the available actions
 * to the console.
 */
public class ListActionsAction extends Action {
	
	public static final String ID = "ListActions";
	
	ListActionsAction() {
		super(ID, Permissions.EVERYONE);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		ArrayList<String> actions = new ArrayList<>(Action.getActions());
		Collections.sort(actions);
		
		executor.sendMessage(manager, "Available actions:");
		for(String action : actions) {
			if(executor.hasPermissions(action))
				executor.sendMessage(manager, " > %s", action);
		}
		
		return 0;
	}
	
}
