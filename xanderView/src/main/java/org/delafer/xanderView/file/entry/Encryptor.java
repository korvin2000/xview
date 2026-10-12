package org.delafer.xanderView.file.entry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;

import org.delafer.xanderView.gui.config.ApplConfiguration;

import net.j7.commons.io.FileUtils;
import net.j7.commons.utils.ByteUtils;


public class Encryptor {
	private static final int ALG_TYPE_BYTES = 1;
	private static final int NAME_LENGTH_BYTES = 2;
	private static final int FILE_LENGTH_BYTES = 4;
	private static final int HEADER_BYTES = ALG_TYPE_BYTES  + FILE_LENGTH_BYTES;

	private static final byte ALGHORITM_AES_128_ECB_NP = (byte)1;

	static final Charset utf8;

	static Cipher encrypt;
	static Cipher decrypt;
	static Key sks;
	static {
		utf8 = Charset.forName("UTF-8");
		Security.setProperty("crypto.policy", "unlimited");
		encrypt = getCipher(true);
		decrypt = getCipher(false);
	}

	public static byte[] randomKey(int size) {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[size];
		random.nextBytes(bytes);
		return bytes;
	};

	private synchronized static Cipher getCipher(boolean mode) {
		Cipher r = null;
		try {
			r = Cipher.getInstance("AES_128/ECB/NoPadding", "SunJCE");
//			r = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
			//			SecretKeyFactory f = SecretKeyFactory.getInstance("AES", "SunJCE");
			String pwd = ApplConfiguration.instance().pwd();
			if (pwd == null || pwd.isEmpty()) pwd = "start123";
			sks = CipherKey.getPBEKey(pwd);
			//			SecretKey key = f.generateSecret(sks);
			r.init(mode ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, sks);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	public Encryptor() {
	}

	public static synchronized ByteBuffer encrypt(DecData data) {
		//		try {
		//			encrypt.init(Cipher.ENCRYPT_MODE, sks/*,  new IvParameterSpec(CipherKey.iv)*/);
		//		} catch (InvalidKeyException /*| InvalidAlgorithmParameterException*/ e) {
		//			e.printStackTrace();
		//		}
		return encrypt0(encrypt, data);
	}

	public static synchronized DecData decrypt(ByteBuffer input) {
		//		try {
		//			decrypt.init(Cipher.DECRYPT_MODE, sks);
		//		} catch (InvalidKeyException e) {
		//			e.printStackTrace();
		//		}
		return decrypt0(decrypt, input);
	}

	private static ByteBuffer header(byte[] name) {
		ByteBuffer bb  = ByteBuffer.allocate(NAME_LENGTH_BYTES + name.length);
		bb.put(ByteUtils.UIntToByte2(name.length));
		bb.put(name);
		bb.rewind();
		return bb;
	}


	public static ByteBuffer encrypt0(Cipher cipher, DecData data) {
		if (data.get() == null || !data.get().hasRemaining()) return data.get();
		try {
			byte[] encName = FileUtils.getFileName(data.name()).getBytes(utf8);
			int fileSize = data.get().remaining();
			int inputSize = fileSize + NAME_LENGTH_BYTES + encName.length;
			
			int validSize = getValidSize(inputSize);
			if (inputSize != validSize) {
//				System.out.println(inputSize+" <> "+validSize);
				encName = extend(encName, (validSize-inputSize));
				inputSize = validSize;
			}
//			System.out.println(">"+inputSize);
			ByteBuffer output = ByteBuffer.allocate(HEADER_BYTES+cipher.getOutputSize(inputSize));
			
			output.putInt(fileSize);
			output.put(Encryptor.ALGHORITM_AES_128_ECB_NP);

			//       cipher.update(ByteBuffer.wrap(new byte[] {1}), output);
			//       cipher.update(ByteBuffer.wrap(ByteUtils.UIntToByte2(encName.length)), output);
			//       cipher.update(ByteBuffer.wrap(encName), output);
			cipher.update(header(encName), output);

			//		// OpensslCipher#update will maintain crypto context.
			//		int n = cipher.update(input, output);
			//		if (n < input.remaining()) {
			//			cipher.doFinal(input, output);
			//		}
			//		else {
			cipher.doFinal(data.get(), output);
			//		}

			output.rewind(); //.flip();
			return output;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static DecData decrypt0(Cipher cipher, ByteBuffer input) {
		if (input == null || !input.hasRemaining()) return new DecData(input, null, 0);
		try {

			int fsize = input.getInt();
//			System.out.println("File size: "+fsize);
			
			byte algType = input.get();
//			System.out.println("Alg type: "+algType);

			int inputSize = input.remaining();
			ByteBuffer output = ByteBuffer.allocate(cipher.getOutputSize(inputSize));

			cipher.doFinal(input, output);

			output.rewind();
			byte[] len = new byte[2];
			output.get(len, 0, 2);
			int length = ByteUtils.Byte2ToUInt(len);
			byte[] name = new byte[length];
			output.get(name, 0, length);

			output = output.slice();
			return new DecData(output, asString(name), fsize);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	//	 public static ByteBuffer encrypt(ByteBuffer clear) {
	//		    if (clear == null || !clear.hasRemaining()) return clear;
	//		    try {
	//		      SecretKeySpec sKeySpec = new SecretKeySpec(getRawKey(encryptionSeed), "AES");
	//		      Cipher cipher = Cipher.getInstance("AES");
	//		      cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
	//		      ByteBuffer encrypted = ByteBuffer.allocate(cipher.getOutputSize(clear.remaining()));
	//		      cipher.doFinal(clear, encrypted);
	//		      encrypted.rewind(); //.flip();
	//		      return encrypted;
	//		    } catch (Exception e) {
	//		      throw new IllegalStateException(e);
	//		    }
	//		  }
	//
	//		  public static ByteBuffer decrypt(ByteBuffer encrypted) {
	//		    if (encrypted == null || !encrypted.hasRemaining()) return encrypted;
	//		    try {
	//		      SecretKeySpec sKeySpec = new SecretKeySpec(getRawKey(encryptionSeed), "AES");
	//		      Cipher cipher = Cipher.getInstance("AES");
	//		      cipher.init(Cipher.DECRYPT_MODE, sKeySpec);
	//		      ByteBuffer decrypted = ByteBuffer.allocate(cipher.getOutputSize(encrypted.remaining()));
	//		      cipher.doFinal(encrypted, decrypted);
	//		      decrypted.rewind(); //.flip();
	//		      return decrypted;
	//		    } catch (Exception e) {
	//		      throw new IllegalStateException(e);
	//		    }
	//		  }

	public static class DecData {

		private String name;
		private int size;
		private ByteBuffer bb;
		private Buf buf;

		public DecData(Buf buf, String name, int size) {
			this(buf.get(), name, size);
			this.buf = buf;
		}

		public DecData(ByteBuffer bb, String name, int size) {
			this.bb = bb;
			this.name = name;
			this.size = size;
		}

		public String name() {
			return this.name;
		}

		public ByteBuffer get() {
			return this.bb;
		}
		
		public int size() {
			return this.size;
		}

		public void close() throws IOException  {
			if (null != buf) {
				buf.close();
				buf = null;
			} else
			if (null != bb) {
				HelperFS.closeDirectBuffer(bb);
			}
			this.bb = null;
		}

	}
	
	private static int getValidSize(int x) {
		int z = ((x+15) >> 4) << 4;
		return z;
	}
	
	private static byte[] extend(byte[] input, int increment) {
		byte[] arr = new byte[input.length+increment];
		System.arraycopy(input, 0, arr, 0, input.length);
		return arr;
	}
	
	private static String asString(byte[] b) {
		   int len;
		   if (null != b && (len = b.length)!=0)
		   while (len > 0) if (b[--len]!=0) {
//			   final byte[] str = new byte[++len];
//			   System.arraycopy(b, 0, str, 0, len);
			   return new String(b, 0, (++len), utf8);
		   }
		   return null;
	}
	

}
