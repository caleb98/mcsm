package ccode.mcsm.action;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.Block;
import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.scheduling.Scheduler;

/**
 * An action that creates a specified duration delay in the execution of
 * a task sequence. Has no effect if ran from the command line directly
 * (although it will still create a scheduler entry.)
 */
public class DelayAction extends Action {

	public static final String ID = "Delay";
	
	public static final Pattern ARGUMENT_PATTERN = Pattern.compile("(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?");
	
	DelayAction() {
		super(ID, Permissions.MODERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String delay) {
		Matcher m = ARGUMENT_PATTERN.matcher(delay);
		
		if(!m.find()) {
			executor.sendMessage(manager, "Error in Delay: provided arguments don't match expected input.");
			return -1;
		}
		
		String hoursString = m.group(1);
		String minutesString = m.group(2);
		String secondsString = m.group(3);
		
		int hours = (hoursString == null) ? 0 : Integer.valueOf(hoursString);
		int minutes = (minutesString == null) ? 0 : Integer.valueOf(minutesString);
		int seconds = (secondsString == null) ? 0 : Integer.valueOf(secondsString);
		
		long delayTime = seconds + (60 * minutes) + (3600 * hours);
		
		final Block block = new Block();
		
		Scheduler.schedule(()->{
			synchronized(block) {
				block.unblock();
				block.notify();
			}
		}, delayTime, TimeUnit.SECONDS);
		
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
