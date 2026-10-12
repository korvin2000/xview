package org.delafer.xanderView.scale.deprecated;

import net.j7.commons.types.BitNumber;

public class SomeTest {

	public SomeTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		float a = 2.22222f;

		int bits1 = Float.floatToRawIntBits(a);
		int bits2 = Float.floatToRawIntBits(a * 2f);
		System.out.println(new BitNumber(bits1));
		System.out.println(new BitNumber(bits2));

		System.out.println("___");

		float b = 1312.99f;

		int xits1 = Float.floatToRawIntBits(b);
		int xits2 = Float.floatToRawIntBits(b * 2f);
		System.out.println(new BitNumber(xits1));
		System.out.println(new BitNumber(xits2));


	}

}
