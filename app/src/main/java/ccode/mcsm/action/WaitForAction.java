package ccode.mcsm.action;

import ccode.mcsm.Block;
import ccode.mcsm.MinecraftServerManager;

public class WaitForAction extends Action {

	public static final String ID = "WaitFor";
	
	@Override
	public int execute(MinecraftServerManager manager, String event) {
		if(event.length() == 0) {
			System.err.println("Error in WaitFor: invalid arguments provided.");
			return -1;
		}
		
		Block block = new Block();
		
		manager.addListener((e)->{
			//TODO: remove for release
			assert(Thread.currentThread().getName().equals("Event-Processor-Thread"));
			if(e.id.equals(event)) {
				synchronized(block) {
					block.unblock();
					block.notify();
				}
				return true;
			}
			return false;
		});
		
		synchronized(block) {
			while(block.blocking()) {
				try {
					block.wait();
				} catch (InterruptedException e) {}
			}
		}
		
		return 0;
	}
	
}
