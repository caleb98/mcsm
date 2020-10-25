package ccode.mcsm.action;

import java.util.ArrayList;
import java.util.Collections;

import ccode.mcsm.MinecraftServerManager;

/**
 * A utility action that prints each of the available actions
 * to the console.
 */
public class ListActionsAction extends Action {
	
	public static final String ID = "ListActions";
	
	ListActionsAction() {
		super(ID, 4);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, String args) {
		ArrayList<String> actions = new ArrayList<>(Action.getActions());
		Collections.sort(actions);
		
		System.out.println("Available actions:");
		for(String action : actions) {
			System.out.printf("\t%s\n", action);
		}
		return 0;
	}
	
}
