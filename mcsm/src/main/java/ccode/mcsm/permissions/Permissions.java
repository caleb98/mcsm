package ccode.mcsm.permissions;

public enum Permissions implements Comparable<Permissions> {

	MCSM_EXECUTOR(100),		// The MCSM process
	SERVER_OWNER(20),		// The server owner
	SERVER_OPERATOR(15),	// Server operators/co-owners
	MODERATOR(10),			// Server moderators
	TRUSTED(5),				// Trusted players
	EVERYONE(0);			// Default permissions level for everyone on the server
	
	public final int level;
	
	Permissions(int level) {
		this.level = level;
	}
	
}
