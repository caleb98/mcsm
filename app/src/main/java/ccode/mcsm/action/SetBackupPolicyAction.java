package ccode.mcsm.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.backup.MaxCapacityPolicy;
import ccode.mcsm.backup.MaxCountPolicy;
import ccode.mcsm.backup.NoLimitPolicy;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class SetBackupPolicyAction extends Action {

	public static final String ID = "SetBackupPolicy";
	
	public static final Pattern ARGUMENT_PATTERN = Pattern.compile(
			String.format("(%s|%s|%s)([ \\w\\d]+)+",
					NoLimitPolicy.class.getSimpleName(),
					MaxCapacityPolicy.class.getSimpleName(),
					MaxCountPolicy.class.getSimpleName()));
	
	public SetBackupPolicyAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		Matcher m = ARGUMENT_PATTERN.matcher(args);
		if(!m.matches()) {
			sendMessage(manager, executor, "Invalid arguments. Provide a policy name, world name, and any required values for that policy.");
			return -1;
		}
		
		String policyType = m.group(1);
		String policyArgsFull = m.group(2);
		String[] policyArgs = policyArgsFull.trim().split("\\s+");
		
		//Check that world name is provided
		if(policyArgs.length < 1) {
			sendMessage(manager, executor, "Invalid arguments. Provide a policy name, world name, and any required values for that policy.");
			return -1;
		}
		
		//Check for NoLimitPolicy
		if(policyType.equals(NoLimitPolicy.class.getSimpleName())) {
			manager.getBackupManager().getPolicies()
				.put(policyArgs[0], new NoLimitPolicy(manager.getBackupManager()));
		}
		
		//Check for MaxCapacityPolicy
		else if(policyType.equals(MaxCapacityPolicy.class.getSimpleName())) {
			if(policyArgs.length < 2) {
				sendMessage(manager, executor, "Invalid arguments. Please provide the max capacity (in bytes) for this policy.");
				return -1;
			}

			try {
				long maxBytes = Long.parseLong(policyArgs[1]);
				manager.getBackupManager().getPolicies()
					.put(policyArgs[0], new MaxCapacityPolicy(manager.getBackupManager(), maxBytes));
			} catch (NumberFormatException e) {
				sendMessage(manager, executor, "Error reading max bytes value: %s", e.getMessage());
				return -1;
			}
		}
		
		//Check for MaxCountPolicy
		else if(policyType.equals(MaxCountPolicy.class.getSimpleName())) {
			if(policyArgs.length < 2) {
				sendMessage(manager, executor, "Invalid arguments. Please provide the max number of backups for this policy.");
				return -1;
			}
			
			try {
				int maxCount = Integer.parseInt(policyArgs[1]);
				manager.getBackupManager().getPolicies()
					.put(policyArgs[0], new MaxCountPolicy(manager.getBackupManager(), maxCount));
			} catch (NumberFormatException e) {
				sendMessage(manager, executor, "Error reading max backups value: %s", e.getMessage());
				return -1;
			}
		}
		
		//Horrible error, since we matched but didn't have an acceptable policy.
		else {
			sendMessage(manager, executor, "Error setting policy. This error should never be reached, so if you see this message please report it.");
			return -1;
		}
		
		sendMessage(manager, executor, "Policy successfully updated.");
		return 0;
	}
	
}
