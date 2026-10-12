package net.j7.commons.utils;


public final class ByteUtils {

    private static final long MIN_VALUE_B = Long.MIN_VALUE + 1;


//    public static byte[] shortToByteArray(short s) {
//        return new byte[]{(byte)(s & 0x00FF),(byte)((s & 0xFF00)>>8)};
//    }
//
//
//    public static byte[] shortToByteArray2(int s) {
//        return new byte[]{(byte)(s & 0xFF),(byte)((s >> 8) & 0xFF)};
//    }

    public static byte[] UIntToByte2(int value) {
    	if (value < 0) value = 0;
    	if (value > Short.MAX_VALUE) value = Short.MAX_VALUE;
    	value += Short.MIN_VALUE;
		return new byte[] {(byte) (value >>> 8),
		(byte) value};
    }

    public static int Byte2ToUInt(byte[] arr) {
//        return (short)(((arr[0] & 0xFF) << 8) +
//		        (arr[1] & 0xFF));
    	int  x =  (arr[0]<<8 | arr[1] & 0xFF);
    	x -= Short.MIN_VALUE;
    	return x;
    }

//    public static void main(String[] args) {
////    	show(shortToByteArray((short)10000));
////    	show(shortToByteArray3((short)10000));
////    	System.out.println(byteArrayToShort(shortToByteArray3((short)-32768)));
////    	System.out.println(byteArrayToShort(shortToByteArray3((short)-30000)));
////    	System.out.println(byteArrayToShort(shortToByteArray3((short)-10000)));
////    	System.out.println(byteArrayToShort(shortToByteArray3((short)-100)));
////    	System.out.println(byteArrayToShort(shortToByteArray3((short)-1)));
//    	System.out.println(byte2ToInt(intToByte2((short)0)));
//    	System.out.println(byte2ToInt(intToByte2((short)1)));
//    	System.out.println(byte2ToInt(intToByte2((short)100)));
//    	System.out.println(byte2ToInt(intToByte2((short)10000)));
//    	System.out.println(byte2ToInt(intToByte2((short)30000)));
//    	System.out.println(byte2ToInt(intToByte2((short)32767)));
//    	System.out.println(byte2ToInt(intToByte2((short)42767)));
//    	System.out.println(byte2ToInt(intToByte2(65535)));
//    	System.out.println(byte2ToInt(intToByte2(65536)));
////    	int x = Short.MIN_VALUE;
////    	int y = Short.MAX_VALUE;
////    	for (int i = x; i <= y; i++) {
////			if ((short)i != byteArrayToShort(shortToByteArray3((short)i))) System.out.println("error");
////		}
//	}

    public static void show(byte[] arr) {
    	System.out.println(arr[0] + " & "+arr[1]);
    }


	public static int byteArrayToInt(byte[] b) {
        return
        (b[0] << 24) +
        ((b[1] & 0xFF) << 16) +
        ((b[2] & 0xFF) << 8) +
        (b[3] & 0xFF);
    }

    public static byte[] intToByteArray(int value) {
        return new byte[] {
        		(byte) (value >>> 24),
        		(byte) (value >>> 16),
        		(byte) (value >>> 8),
        		(byte) value
        		};
    }

    private static byte byteAt(byte[] b, int p) {
    	if (b != null && p<b.length) return b[p];
    	return 0;
    }

    public static long byteArrayToLong(byte[] b) {
        //8 16 24 32 40 48 56 ..
    	return
        ((long)byteAt(b,0) << 56) +
        ((long)(byteAt(b,1) & 0xFF) << 48) +
        ((long)(byteAt(b,2) & 0xFF) << 40) +
        ((long)(byteAt(b,3) & 0xFF) << 32) +
        ((long)(byteAt(b,4) & 0xFF) << 24) +
        ((long)(byteAt(b,5) & 0xFF) << 16) +
        ((long)(byteAt(b,6) & 0xFF) << 8) +
        ((long)byteAt(b,7) & 0xFF);


    }

    public static byte[] longToByteArray(long value) {
        return new byte[] {
        		(byte) (value >>> 56),
        		(byte) (value >>> 48),
        		(byte) (value >>> 40),
        		(byte) (value >>> 32),
        		(byte) (value >>> 24),
        		(byte) (value >>> 16),
        		(byte) (value >>> 8),
        		(byte) value
        		};



    }

    public static byte[] longObjToByteArray(Long value) {
        return longToByteArray(value != null ? value.longValue() : MIN_VALUE_B);
    }

    public static Long byteArrayToLongObj(byte[] b) {
    	long value = byteArrayToLong(b);
    	return value == MIN_VALUE_B ? null : Long.valueOf(value);
    }

    public static byte[] doubleToByteArray(double value) {
    	long l = Double.doubleToLongBits(value);
    	return longToByteArray(l);
    }

    public static double byteArrayToDouble(byte[] b) {
    	long l = byteArrayToLong(b);
    	return Double.longBitsToDouble(l);
    }


    public static byte[] floatToByteArray(float value) {
    	int l = Float.floatToIntBits(value);
    	return intToByteArray(l);
    }

    public static float byteArrayToFloat(byte[] b) {
    	int l = byteArrayToInt(b);
    	return Float.intBitsToFloat(l);
    }


}
