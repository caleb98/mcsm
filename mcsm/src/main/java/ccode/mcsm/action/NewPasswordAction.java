package ccode.mcsm.action;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import ccode.mcsm.MinecraftServerManager;
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
		super(ID, Permissions.NO_PERMISSIONS);
	}

	@Override
	public int execute(MinecraftServerManager manager, Player executor, String args) {
		String pass = "";
		Random rand = new Random();
		for(int i = 0; i < PASS_LENGTH; i++) {
			pass += PASS_CHARS.charAt(rand.nextInt(PASS_CHARS.length()));
		}
		
		String hash;
		try {
			hash = Hash.hash(pass);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			sendMessage(manager, executor, "Error generating password: %s", e.getMessage());
			return -1;
		}
		
		executor.setPasswordHash(hash);
		sendMessage(manager, executor, "Your new password is: %s", pass);
		
		return 0;
	}
	
}
