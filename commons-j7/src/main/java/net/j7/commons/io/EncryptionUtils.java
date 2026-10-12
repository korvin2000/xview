/*
 * @File: EncryptionUtils.java
 *
 *
 *
 * All rights reserved.
 *
 * @Author:  tavrovsa
 *
 * @Version $Revision: #3 $Date: $
 *
 *
 */
package net.j7.commons.io;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class EncryptionUtils.
 *
 * @author  tavrovsa
 * @version $Revision: #3 $
 */
public final class EncryptionUtils {

   private static final String CHARSET_UTF8 = "UTF-8";

   /** The technical logger to use. */
   private static final Logger logger = LoggerFactory.getLogger(EncryptionUtils.class);

   private final static String DESEDE_ENCRYPTION_SCHEME = "DESede"; //Triple DES Encryption (DES-EDE)

   private final static byte[] keyBytes = new byte[]
         {  (byte)0x44, (byte)0xb2, (byte)0x4b, (byte)0x07, (byte)0xC3, (byte)0xD5,
            (byte)0x13, (byte)0x21, (byte)0x08, (byte)0x09, (byte)0xBC, (byte)0xE4,
            (byte)0xAA, (byte)0xC3, (byte)0x4F, (byte)0xEE, (byte)0x59, (byte)0x11,
            (byte)0xBB, (byte)0x04, (byte)0x58, (byte)0x33, (byte)0x5d, (byte)0x75 };


   private final static byte[] getBytesUtf8(String string) {

      byte[] res = null;
      if (string == null)
         return res;
      try {
         res = string.getBytes(CHARSET_UTF8);
      } catch (UnsupportedEncodingException e) {
         logger.error("",e);
      }
      return res;
   }

   /**
    * Symmetric decryption with Triple DES algorith
    *
    * @see String encryptSymmetric3DES(String plaintext)
    * @param encryptedText as String
    * @return decrypted string
    */
   public static String decryptSymmetric3DES(String encryptedText) {

      try {
         SecretKeySpec key = new SecretKeySpec(keyBytes, DESEDE_ENCRYPTION_SCHEME);
         Cipher cipher = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
         cipher.init(Cipher.DECRYPT_MODE, key);

         // Decode base64 to get bytes

         //byte[] dec = Base64.getDecoder().decode(getBytesUtf8(encryptedText));
         byte[] dec = new byte[0];

         // Decrypt
         byte[] utf8 = cipher.doFinal(dec);

         // Decode using utf-8
         return new String(utf8, "UTF8");

      } catch (Exception e) {
         logger.error("",e);
         return null;
      }
   }

   /**
    * Symmetric encryption with Triple DES algorith
    *
    * @see String decryptSymmetric3DES(String encryptedText)
    * @param plaint text to encrypt
    * @return encrypted string
    */
   public static String encryptSymmetric3DES(String plaintext) {

      try {
         SecretKeySpec key = new SecretKeySpec(keyBytes, DESEDE_ENCRYPTION_SCHEME);
         Cipher cipher = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME);
         cipher.init(Cipher.ENCRYPT_MODE, key);

         // encode the string into a sequence of bytes using the named charset
         // storing the result into a new byte array.
         byte[] utf8 = plaintext.getBytes(CHARSET_UTF8);
         byte[] enc = cipher.doFinal(utf8);

         // encode to base64

         String value = encodeBase64String(enc);

         return value;

      } catch (Exception e) {
         logger.error("",e);
         return null;
      }
   }

   /**
    * @param enc
    * @return
    * @throws UnsupportedEncodingException
    */
   private static String encodeBase64String(byte[] enc) throws UnsupportedEncodingException {
     // return new String((Base64.getEncoder().encode(enc)), CHARSET_UTF8);
	   return null;
   }

   /**
    * Generates MD5 pseudo-encrypted string. (MD5 Alghoritm implements - one-way hash encryption)<br>
    * <br>
    * One-way MD5 Message-Digest Algorithm is a widely used cryptographic<br>
    * hash function that produces a 128-bit (16-byte) hash value <br>
    * <i>MD5 is a one way hash. It isn't meant to be decrypted.</i>
    *
    * @param plaintext
    * @return md5 digest string (24 bytes)
    */
   public static String md5crypt(String plaintext) {

      try {
         MessageDigest algorithm = MessageDigest.getInstance("MD5");
         algorithm.reset();
         algorithm.update(plaintext.getBytes(CHARSET_UTF8));
         byte rawByte[] = algorithm.digest();

         // Encode bytes to base64 to get a hash string
         String value = encodeBase64String(rawByte);

         return value;

      } catch (Exception e) {
         logger.error("",e);
         return null;
      }
   }
}
