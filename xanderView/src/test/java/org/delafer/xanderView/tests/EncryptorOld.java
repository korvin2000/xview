package org.delafer.xanderView.tests;

import java.util.Arrays;

import net.j7.commons.types.BitNumber;
import net.j7.commons.utils.RandomUtil;

public class EncryptorOld {

	private static final int RSM = 0x000000FF;

	public EncryptorOld() {
		// TODO Auto-generated constructor stub
	}

	public static int fastModulo(int dividend, int divisor)
	{
	   return dividend & (divisor - 1);
	}

	public static byte[] encrypt(byte[] in) {
		return in;
	}

	public static byte[] decrypt(byte[] in) {
		return in;
	}


	public static boolean equals(byte[] a1, byte[] a2) {
		return Arrays.equals(a1, a2);
	}


	public static byte shift1(byte a, int to) {
		to = fastModulo(to, 8);
		return (byte)(((RSM & a) >>> to) | (a << (8-to)));
	}

	public static byte shift2(byte a, int to) {
		to = fastModulo(to, 8);
		return (byte)((a << to) | ((RSM & a) >>> (8-to)));
	}

	public static String show(byte a) {
		 return String.format("%8s", Integer.toBinaryString(a & 0xFF)).replace(' ', '0');
	}

	public static void main(String[] args) {


		System.out.println(15 % 8);
		System.out.println(fastModulo(15, 8));

		byte b = -128;


		for (int j = 0; j < 256; j++) {
			System.out.println(b);

			for (int i = 1; i <= 57; i++) {
				byte x1 = shift1(shift2(b, i), i);
				byte x2 = shift2(shift1(b, i), i);
				if (b != x1 || b != x2) {
					System.out.println("ERROR");
					System.exit(0);
				}
			}

			b += (byte)1;
		}
		System.out.println("OK");
//		byte[] arr = new byte[2];
//		RandomUtil.getRandom().nextBytes(arr);
//
//		byte b = arr[1];
//		System.out.println(b);
//		System.out.println(show(b));
//		for (int i = 1; i <= 8; i++) {
//			System.out.println(i+">> "+show((byte)(b << i)) + " vs "+ show((byte)(b >> i))+" : "+show((byte)(b >>> i)));
//			byte x = (byte)-b;
//			System.out.println(show((byte)(x >> i))+" ; "+show((byte)((RSM & b) >>> i))+" ; "+show((byte)((b >> i)& 0xFF)));
//		}
//		byte a = (byte)64+32+4+2+1;
//		System.out.println(show(a));
//
//		for (int i = 1; i < 9; i++) {
//			byte b=  shift2(a, i);
//			System.out.println(show(b));
//
//			byte b2=  shift2(a, i+8);
//			System.out.println(show(b2));
//		}
//
//
//		System.exit(0);
//		byte[] arr = RandomUtil.generateRandomString(10).getBytes();
//
//		byte[] enc = encrypt(arr);
//		byte[] dec = decrypt(enc);
//
//
//		if (!equals(arr, enc) && equals(arr, dec))
//			System.out.println("PASSED");
//		else
//			System.out.println("NOT PASSED");
	}

}
