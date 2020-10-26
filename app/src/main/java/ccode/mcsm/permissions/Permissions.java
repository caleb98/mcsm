package ccode.mcsm.permissions;

public enum Permissions implements Comparable<Permissions> {

	MCSM_EXECUTOR(1000),
	SERVER_OPERATOR(100),
	SERVER_MODERATOR(10),
	LEVEL_4(4),
	LEVEL_3(3),
	LEVEL_2(2),
	LEVEL_1(1),
	LEVEL_0(0),
	NO_PERMISSIONS(0);
	
	public final int level;
	
	Permissions(int level) {
		this.level = level;
	}
	
}
