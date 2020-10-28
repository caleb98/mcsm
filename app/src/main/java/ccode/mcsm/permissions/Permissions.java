package ccode.mcsm.permissions;

public enum Permissions implements Comparable<Permissions> {

	MCSM_EXECUTOR(1000),	//The MCSM process itself
	SERVER_OWNER(100),		//The server owner
	SERVER_OPERATOR(10),	//Any operators (co-owners)
	MODERATOR(4),			//Moderators
	LEVEL_3(3),				
	LEVEL_2(2),
	LEVEL_1(1),
	LEVEL_0(0),
	NO_PERMISSIONS(0);		//No permissions at all. Default level
	
	public final int level;
	
	Permissions(int level) {
		this.level = level;
	}
	
}
