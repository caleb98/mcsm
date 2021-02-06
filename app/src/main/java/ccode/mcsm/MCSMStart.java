package ccode.mcsm;

import java.io.IOException;

import ccode.mcsm.action.Action;
import ccode.mcsm.task.Tasks;

public class MCSMStart {
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.out.println("Usage: mcremote.jar <server jarfile path>");
			System.exit(0);
		}
		
		//Load the actions
		Action.init();
		
		//Load the tasks from the user file
		try {
			Tasks.loadTasks();
		} catch (IOException e) {
			System.err.printf("Unable to load tasks: %s\n", e.getMessage());
		}
		
		MinecraftServerManager mcsm = new MinecraftServerManager(args[0]);
		mcsm.start();
		
	}
}
