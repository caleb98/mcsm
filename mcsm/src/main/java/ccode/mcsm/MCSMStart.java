package ccode.mcsm;

import java.io.IOException;

import com.esotericsoftware.kryonet.Server;

import ccode.mcsm.action.Action;
import ccode.mcsm.net.KryoCreator;
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
		
		//Start MCSM
		MinecraftServerManager mcsm = new MinecraftServerManager(args[0]);
		mcsm.start();
		
		try {
			Server remote = KryoCreator.createServer();
			remote.addListener(mcsm);
			remote.start();
			remote.bind(36363, 36363);
		} catch (IOException e) {
			System.err.printf("Error creating kryo server: %s\n", e.getMessage());
		}
		
	}
}
