package org.delafer.xanderView.file.entry;

import java.io.File;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelperFS {

	private static final Logger log = LoggerFactory.getLogger(HelperFS.class);

	public final static boolean isVirtualDirectory(String path) {
		if (path == null || path.isEmpty())  return false;
		return ImageAbstract.ImageType.XAV.equals(ImageAbstract.getType(path));
	}

	public final static boolean isAnyDirectory(File file) {
		return file != null && (file.isDirectory() || isVirtualDirectory(file.getPath()));
	}

	public final static boolean isAnyFile(File file) {
		return file != null && !isAnyDirectory(file);
	}

	/** Maps [0, size) of the file read-only (size < 0 or past EOF: the whole file) until the returned Buf is closed. */
	public final static Buf readDataBuf(String fileName, int size) throws IOException {
		// The channel may close right away: a mapping does not depend on it.
		try (FileChannel fc = FileChannel.open(Path.of(fileName), StandardOpenOption.READ)) {
			final long fileSize = fc.size();
			// size < 0 means "map the whole file"; otherwise never map past EOF - a read-only
			// mapping larger than the file fails on some platforms. A file shorter than the
			// requested hash window is simply mapped in full, which yields the same hash.
			final long len = size < 0 || size > fileSize ? fileSize : size;
			return arenaBuf(arena -> fc.map(MapMode.READ_ONLY, 0, len, arena));
		} catch (IOException e) {
			log.warn("filename: {} size: {}", fileName, size);
			throw e;
		}
	}

	public final static byte[] readData(String fileName, int size) throws IOException {
		try (Buf buf = readDataBuf(fileName, size)) {
			return buf.getArray(false);
		}
	}

	interface SegmentSource {
		MemorySegment get(Arena arena) throws IOException;
	}

	/**
	 * Off-heap memory (a file mapping or a native allocation) owned by a shared FFM arena that the returned Buf closes:
	 * freed (unmapped) deterministically without the terminally deprecated {@code Unsafe.invokeCleaner}; Windows keeps a
	 * mapped file locked until then. Any thread may close it; access after close throws instead of crashing the JVM.
	 */
	static Buf arenaBuf(SegmentSource source) throws IOException {
		final Arena arena = Arena.ofShared();
		try {
			return new Buf(source.get(arena).asByteBuffer(), arena::close);
		} catch (Throwable t) {
			arena.close();
			throw t;
		}
	}

	public static int min(long a1, long a2) {
		return (int) (a1 < a2 ? a1 : a2);
	}
}
