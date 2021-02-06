package ccode.mcsm.permissions;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Hash {
	
	private static final int ITERATIONS = 65536;
	private static final int KEY_LENGTH = 128;
	
	public static String hash(String value) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		//Create salt
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		
		//Hash with PBKDF
		KeySpec spec = new PBEKeySpec(value.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = factory.generateSecret(spec).getEncoded();
		
		//Hash
		return ITERATIONS + ":" + toHex(salt) + ":" + toHex(hash);
		
	}
	
	public static boolean verify(String original, String stored) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String[] parts = stored.split(":");
		int iterations = Integer.parseInt(parts[0]);
		byte[] salt = fromHex(parts[1]);
		byte[] hash = fromHex(parts[2]);
		
		PBEKeySpec spec = new PBEKeySpec(original.toCharArray(), salt, iterations, hash.length * 8);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] testHash = factory.generateSecret(spec).getEncoded();
		
		int diff = hash.length ^ testHash.length;
		for(int i = 0; i < hash.length && i < testHash.length; i++) {
			diff |= hash[i] ^ testHash[i];
		}
		return diff == 0;
	}
	
	private static byte[] fromHex(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for(int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}
	
	private static String toHex(byte[] array) {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if(paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		}
		else {
			return hex;
		}
	}
	
}
