package ccode.mcsm.action;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import ccode.mcsm.MinecraftServerManager;
import ccode.mcsm.permissions.Executor;
import ccode.mcsm.permissions.Hash;
import ccode.mcsm.permissions.Permissions;
import ccode.mcsm.permissions.Player;

public class NewPasswordAction extends Action {

	public static final String ID = "NewPassword";
	
	private static final int PASS_LENGTH = 8;
	private static final String PASS_CHARS = 
			"abcdefghijklmnopqrstuvwxyz" +
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
			"1234567890" +
			"!@#$%^&*()-+=";
	
	public NewPasswordAction() {
		super(ID, Permissions.EVERYONE);
	}

	@Override
	public int execute(MinecraftServerManager manager, Executor executor, String args) {
		if(!(executor instanceof Player)) {
			executor.sendMessage(manager, "This action can only be performed via in-game mcsm commands.");
			return -1;
		}
		
		Player player = (Player) executor;
		
		String pass = "";
		Random rand = new Random();
		for(int i = 0; i < PASS_LENGTH; i++) {
			pass += PASS_CHARS.charAt(rand.nextInt(PASS_CHARS.length()));
		}
		
		String hash;
		try {
			hash = Hash.hash(pass);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			executor.sendMessage(manager, "Error generating password: %s", e.getMessage());
			return -1;
		}
		
		player.setPasswordHash(hash);
		player.sendMessage(manager, "Your new password is: %s", pass);
		
		return 0;
	}
	
}
