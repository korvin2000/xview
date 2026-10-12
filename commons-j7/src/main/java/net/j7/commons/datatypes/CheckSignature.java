package net.j7.commons.datatypes;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.security.Signature;
import java.util.Arrays;
import java.util.Stack;

import static net.j7.commons.datatypes.CheckSignature.Signature.of;
public class CheckSignature {

	public static FileType WEBP     = FileType.of("webp",   of(8, 0x57, 0x45, 0x42, 0x50));
	public static FileType SEVZIP   = FileType.of("7z",     of(0, 0x37, 0x7A, 0xBC, 0xAF));
	public static FileType AVIF     = FileType.of("avif",   of(4, 0x66, 0x74, 0x79, 0x70));
	public static FileType JXL      = FileType.of("jxl",    of(0, 0xFF, 0x0A), of(0, 0x00, 0x00, 0x00, 0x0C, 0x4A));
	public static FileType JPG      = FileType.of("jpg",    of(0, 0xFF, 0xD8));
	public static FileType PNG      = FileType.of("png",    of(0, 0x89, 0x50, 0x4E, 0x47));
	public static FileType GIF      = FileType.of("gif",    of(0, 0x47, 0x49, 0x46, 0x38));
	public static FileType WEBP2    = FileType.of("wp2",    of(0, 0xF4, 0xFF, 0x6F));
	public static FileType HEIF     = FileType.of("heif",   of(5, 0x74, 0x79, 0x70, 0x68, 0x65, 0x69),
																of(5, 0x74, 0x79, 0x70, 0x68, 0x65, 0x76),
																of(5, 0x74, 0x79, 0x70, 0x6D, 0x69, 0x66));

	final static FileType[] sigArr = new FileType[] { WEBP, SEVZIP, AVIF, JXL, JPG, PNG, GIF, WEBP2, HEIF };

	public static void main(String[] args) {
		Signature a = of( 0, 0x00, 0x10, 0x0C, 0x1A, 0x37, 0x7A, 0x33, 0x0C, 0xD0, 0xAF, 0xBC, 0xF4, 0xFF);
		byte[] z = a.signature();
		for (int i = 0; i < z.length; i++) {
			System.out.println((String.format("%02x", z[i])));
		}
	}

	private static Signature nextStack(Stack<Signature> stack) {
		return stack.isEmpty() ? null : stack.pop();
	}

	public static boolean checkImageSpi( ImageInputStream stream, FileType fileType ) throws IOException {
		Stack<Signature> stack = new Stack<Signature>();
		for (Signature signature : fileType.sign())  stack.push(signature);
		ByteOrder oldByteOrder = stream.getByteOrder();
		stream.setByteOrder( ByteOrder.LITTLE_ENDIAN );
		Signature next = nextStack(stack);
		while (next != null) {
			int size = next.signature().length, shift = next.offset;
			byte[] b = new byte[ size ];
			stream.mark();
			if (next.offset > 0)  stream.skipBytes(shift);
			try {
				stream.readFully( b );
				do {
					if ( Arrays.equals( b, next.signature ) )  {
						stream.setByteOrder( oldByteOrder );
						return true;
					}
					next = nextStack(stack);
				} while ( next != null && size == next.signature.length &&  shift == next.offset );
			} finally {
				stream.reset();
			}
		}
		stream.setByteOrder( oldByteOrder );
		return false;
	}


	protected record FileType(String ext, Signature[] sign) {
		static FileType of(String ext, Signature... sign) {
			return new FileType(ext, sign);
	}}

	protected record Signature(int offset, byte[] signature) {
		static Signature of(int offset, int... signature) {
			return new Signature(offset, toByte(signature));
		}
		static byte[] toByte(int[] arr) {
			int i = arr.length; byte[] ret = new byte[i];
			while (--i >= 0) { ret[i] = (byte) (arr[i] & 0xFF); }
			return ret;
	}}

}

