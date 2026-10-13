package org.delafer.xanderView.codec;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.function.IntBinaryOperator;

import net.j7.commons.jni.LibraryLoader;

/**
 * JPEG decoding through the TurboJPEG 3 C API ({@code libturbojpeg} 3.0 or newer) over the FFM API: no JNI glue, so any
 * upstream or custom build of the library works. The image is decoded straight into the {@link BufferedImage}'s byte
 * array (zero-copy, as the former JNI wrapper did through {@code GetPrimitiveArrayCritical}): the two downcalls that take
 * heap arrays are {@linkplain Linker.Option#critical(boolean) critical}, so a safepoint requested by another thread waits
 * until the decode returns (about 0.1 s per 24 MP; the UI is blocked by the decode anyway, PLAN P3.3). Measured on
 * Windows x64 with JDK 26 (docs/DEPENDENCIES.md §4): equal to the JNI wrapper within 1 %; an off-heap destination plus
 * a copy would cost 8-20 %.
 */
public final class TurboJpeg {

	private static final int INIT_DECOMPRESS = 1, PF_BGR = 1;
	private static final int PARAM_JPEGWIDTH = 5, PARAM_JPEGHEIGHT = 6, PARAM_FASTUPSAMPLE = 9, PARAM_FASTDCT = 10;
	private static final int ERROR_STRING_MAX = 200; // JMSG_LENGTH_MAX
	private static final ValueLayout.OfInt INT = ValueLayout.JAVA_INT;
	private static final StructLayout SCALING_FACTOR = MemoryLayout.structLayout(INT.withName("num"), INT.withName("denom"));

	/** Bound on first use; a missing native (LibraryLoader soft-fails) surfaces as an {@link UnsatisfiedLinkError} here. */
	private static final class Lib {
		static final MethodHandle INIT, DESTROY, ERROR, SET, GET, HEADER, SCALING, DECOMPRESS;
		static {
			LibraryLoader.loadLibrary("turbojpeg");
			Linker l = Linker.nativeLinker();
			SymbolLookup s = SymbolLookup.loaderLookup();
			ValueLayout.OfLong SIZE_T = ValueLayout.JAVA_LONG;
			INIT = bind(l, s, "tj3Init", FunctionDescriptor.of(ValueLayout.ADDRESS, INT));
			DESTROY = bind(l, s, "tj3Destroy", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
			ERROR = bind(l, s, "tj3GetErrorStr", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
			SET = bind(l, s, "tj3Set", FunctionDescriptor.of(INT, ValueLayout.ADDRESS, INT, INT));
			GET = bind(l, s, "tj3Get", FunctionDescriptor.of(INT, ValueLayout.ADDRESS, INT));
			SCALING = bind(l, s, "tj3SetScalingFactor", FunctionDescriptor.of(INT, ValueLayout.ADDRESS, SCALING_FACTOR));
			HEADER = bind(l, s, "tj3DecompressHeader", FunctionDescriptor.of(INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, SIZE_T),
					Linker.Option.critical(true));
			DECOMPRESS = bind(l, s, "tj3Decompress8",
					FunctionDescriptor.of(INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, SIZE_T, ValueLayout.ADDRESS, INT, INT),
					Linker.Option.critical(true));
		}

		private static MethodHandle bind(Linker l, SymbolLookup s, String name, FunctionDescriptor d, Linker.Option... options) {
			MemorySegment symbol = s.find(name).orElseThrow(() -> new UnsatisfiedLinkError("turbojpeg: no symbol " + name));
			return l.downcallHandle(symbol, d, options);
		}
	}

	private TurboJpeg() {}

	/**
	 * Decodes a JPEG to {@link BufferedImage#TYPE_3BYTE_BGR}.
	 * @param denominator picks the DCT scaling denominator (1, 2, 4 or 8) from the full width and height
	 * @param fastDct use the fast (less accurate) DCT and chroma upsampling
	 */
	public static BufferedImage decode(byte[] jpeg, IntBinaryOperator denominator, boolean fastDct) throws IOException {
		try {
			return decode0(jpeg, denominator, fastDct);
		} catch (IOException | RuntimeException | Error e) {
			throw e;
		} catch (Throwable t) {
			throw new IOException(t);
		}
	}

	private static BufferedImage decode0(byte[] jpeg, IntBinaryOperator denominator, boolean fastDct) throws Throwable {
		MemorySegment h = (MemorySegment) Lib.INIT.invokeExact(INIT_DECOMPRESS);
		if (h.equals(MemorySegment.NULL)) throw new IOException("tj3Init failed");
		try (Arena arena = Arena.ofConfined()) {
			MemorySegment src = MemorySegment.ofArray(jpeg);
			check(h, (int) Lib.HEADER.invokeExact(h, src, (long) jpeg.length));
			int width = (int) Lib.GET.invokeExact(h, PARAM_JPEGWIDTH), height = (int) Lib.GET.invokeExact(h, PARAM_JPEGHEIGHT);
			int denom = denominator.applyAsInt(width, height);
			if (denom != 1) {
				MemorySegment sf = arena.allocate(SCALING_FACTOR);
				sf.set(INT, 0, 1);
				sf.set(INT, 4, denom);
				check(h, (int) Lib.SCALING.invokeExact(h, sf));
			}
			if (fastDct) {
				check(h, (int) Lib.SET.invokeExact(h, PARAM_FASTDCT, 1));
				check(h, (int) Lib.SET.invokeExact(h, PARAM_FASTUPSAMPLE, 1));
			}
			int w = scaled(width, denom), ht = scaled(height, denom); // TJSCALED
			BufferedImage img = new BufferedImage(w, ht, BufferedImage.TYPE_3BYTE_BGR);
			byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
			check(h, (int) Lib.DECOMPRESS.invokeExact(h, src, (long) jpeg.length, MemorySegment.ofArray(pixels), w * 3, PF_BGR));
			return img;
		} finally {
			Lib.DESTROY.invokeExact(h);
		}
	}

	private static int scaled(int dimension, int denom) {
		return (dimension + denom - 1) / denom;
	}

	private static void check(MemorySegment h, int rc) throws Throwable {
		if (rc < 0) {
			MemorySegment msg = (MemorySegment) Lib.ERROR.invokeExact(h);
			throw new IOException("TurboJPEG: " + msg.reinterpret(ERROR_STRING_MAX).getString(0));
		}
	}
}
