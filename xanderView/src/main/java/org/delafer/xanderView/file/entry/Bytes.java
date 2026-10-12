package org.delafer.xanderView.file.entry;

public class Bytes {

//	public static void main(String[] args) {
//		int x = 193251;
//		for (int i = 0; i < 255; i++) {
//			System.out.println(i+" > "+getValidSize(i));	
//		}
//		
//
//	}

	private static int getValidSize(int x) {
		int z = ((x+15) >> 4) << 4;
		return z;
	}
	
	private static byte[] extend(byte[] input, int size) {
		byte[] arr = new byte[size];
		System.arraycopy(input, 0, arr, 0, input.length);
		return arr;
	}

}
