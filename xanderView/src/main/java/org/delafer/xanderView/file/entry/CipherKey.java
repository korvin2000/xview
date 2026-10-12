package org.delafer.xanderView.file.entry;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherKey {

	public static byte[] iv = { (byte) 0x19, (byte) 0x73, (byte) 0x41, (byte) 0x8c, (byte) 0x7e, (byte) 0xd8, (byte) 0xee, (byte) 0x89, (byte) 0x19, (byte) 0x73, (byte) 0x41, (byte) 0x8c, (byte) 0x7e, (byte) 0xd8, (byte) 0xee, (byte) 0x89 };

	public CipherKey() {
	}

	final static String pwd_salt = "z!a3";
	final static int keySize = 16;

	public static Key getKey(String keyStr) {
		try {

			// Das Passwort bzw der Schluesseltext
			// byte-Array erzeugen
			byte[] key = (pwd_salt+keyStr).getBytes("UTF-8");
			// aus dem Array einen Hash-Wert erzeugen mit MD5 oder SHA
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			key = sha.digest(key);
			// nur die ersten 128 bit nutzen
			key = Arrays.copyOf(key, keySize);
			// der fertige Schluessel
			SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
			return secretKeySpec;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static Key getPBEKey(String password) {
		try {

			// Get the salt
			String salt = pwd_salt;
			byte[] saltBytes = salt.getBytes("UTF-8"); // Convert bytes to string

			// Derive the key
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 1024, keySize * 8);

			SecretKey secretKey = factory.generateSecret(spec);
			SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
			return secret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}


//	public static Key getPBEKey2(String password) {
//		try {
//
//			 PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), pwd_salt.getBytes(), 2048, keySize * 8);
//		        SecretKey derivedKey = new PBKDF2KeyImpl(keySpec, "HmacSHA1");
//
//		        SecretKeySpec key = new SecretKeySpec(Array.copy(derivedKey.getEncoded(), 0, 16), "AES");
//			return secret;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//
//	}

}
