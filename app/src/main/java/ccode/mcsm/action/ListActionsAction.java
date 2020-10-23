package ccode.mcsm.action;

import java.util.ArrayList;
import java.util.Collections;

import ccode.mcsm.MinecraftServerManager;

public class ListActionsAction extends Action {
	
	public static final String ID = "ListActions";
	
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
