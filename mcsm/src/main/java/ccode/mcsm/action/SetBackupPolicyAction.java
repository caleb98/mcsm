package ccode.mcsm.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.backup.MaxCapacityPolicy;
import ccode.mcsm.backup.MaxCountPolicy;
import ccode.mcsm.backup.NoLimitPolicy;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Permissions;

public class SetBackupPolicyAction extends Action {

	public static final String ID = "SetBackupPolicy";
	
	public static final Pattern ARGUMENT_PATTERN = Pattern.compile(
			String.format("([\\w\\d]+) (\\w+)([ \\w\\d]+)*",
					NoLimitPolicy.class.getSimpleName(),
					MaxCapacityPolicy.class.getSimpleName(),
					MaxCountPolicy.class.getSimpleName()));
	
	public SetBackupPolicyAction() {
		super(ID, Permissions.SERVER_OPERATOR);
	}
	
	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		Matcher m = ARGUMENT_PATTERN.matcher(args);
		if(!m.matches()) {
			executor.sendMessage(manager, "Invalid arguments. Provide a world name, policy name, and any required values for that policy.");
			return -1;
		}
		
		String worldName = m.group(1);
		String policyType = m.group(2);
		String policyArgsFull = m.group(3);
		String[] policyArgs = policyArgsFull == null ? new String[]{} : policyArgsFull.trim().split("\\s+");
		
		//Check for NoLimitPolicy
		if(policyType.equals(NoLimitPolicy.class.getSimpleName())) {
			manager.getBackupManager().getPolicies()
				.put(worldName, new NoLimitPolicy(manager.getBackupManager()));
		}
		
		//Check for MaxCapacityPolicy
		else if(policyType.equals(MaxCapacityPolicy.class.getSimpleName())) {
			if(policyArgs.length < 1) {
				executor.sendMessage(manager, "Invalid arguments for this policy type. Expected max capacity (in bytes).");
				return -1;
			}

			try {
				long maxBytes = Long.parseLong(policyArgs[0]);
				manager.getBackupManager().getPolicies()
					.put(worldName, new MaxCapacityPolicy(manager.getBackupManager(), maxBytes));
			} catch (NumberFormatException e) {
				executor.sendMessage(manager, "Error reading max bytes value: %s", e.getMessage());
				return -1;
			}
		}
		
		//Check for MaxCountPolicy
		else if(policyType.equals(MaxCountPolicy.class.getSimpleName())) {
			if(policyArgs.length < 1) {
				executor.sendMessage(manager, "Invalid arguments for this policy type. Expected maximum number of backups.");
				return -1;
			}
			
			try {
				int maxCount = Integer.parseInt(policyArgs[0]);
				manager.getBackupManager().getPolicies()
					.put(worldName, new MaxCountPolicy(manager.getBackupManager(), maxCount));
			} catch (NumberFormatException e) {
				executor.sendMessage(manager, "Error reading max backups value: %s", e.getMessage());
				return -1;
			}
		}
		
		else {
			executor.sendMessage(manager, "Unable to set policy. Policy type " + policyType + " does not exist.");
			return -1;
		}
		
		executor.sendMessage(manager, "Policy successfully updated.");
		return 0;
	}
	
}
