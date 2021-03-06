package ccode.mcsm.action;

import ccode.mcsm.Block;
import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

/**
 * An action that blocks the execution of a Task list until a specified
 * event is fired. As of right now, this only allows for checking that
 * a specific event is fired. The properties of the event itself are
 * invisible to this action. 
 */
public class WaitForAction extends Action {

	public static final String ID = "WaitFor";
	
	WaitForAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String event) {
		if(event.length() == 0) {
			executor.sendMessage(manager, "Error in WaitFor: invalid arguments provided.");
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
