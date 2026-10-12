package org.delafer.xanderView.file.entry;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
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

	public final static Buf readDataBuf(String fileName, int size) throws IOException {

		final RandomAccessFile fis = new RandomAccessFile(fileName, "r");
		final FileChannel fc = fis.getChannel();
		final long fileSize = fc.size();
		// size < 0 means "map the whole file"; otherwise never map past EOF - a read-only
		// mapping larger than the file fails on some platforms. A file shorter than the
		// requested hash window is simply mapped in full, which yields the same hash.
		if (size < 0 || size > fileSize) {
			size = (int) fileSize;
		}

		MappedByteBuffer buf = null;
		try {
			buf = fc.map(MapMode.READ_ONLY, 0, size);
		} catch (java.io.IOException e) {
			log.warn("filename: {} size: {}", fileName, size);
			throw e;
		}

		return new Buf(buf, fis);
	}


	public final static byte[] readData(String fileName, int size) throws IOException {

		final RandomAccessFile fis = new RandomAccessFile(fileName, "r");
		final FileChannel fc = fis.getChannel();
		if (size < 0) {
			size = (int)fc.size();
		}
		MappedByteBuffer buf = fc.map(MapMode.READ_ONLY, 0, size);
        final byte[] bytes = new byte[size];
        buf.get(bytes);
        fc.force(true);

        fis.close();
        closeDirectBuffer(buf);

        return bytes;
	}

	@SuppressWarnings("restriction")
	public static void closeDirectBuffer(ByteBuffer cb) {
	    if (!cb.isDirect()) return;
//      java < 9
//	    try {
//	    	((sun.nio.ch.DirectBuffer)cb).cleaner().clean();
//	    } catch(Exception ex) { }
//	    cb = null;

        try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			Object unsafe = unsafeField.get(null);
			Method invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
			invokeCleaner.invoke(unsafe, cb);
        } catch (ReflectiveOperationException e) {

			boolean skip = false;
			if (e instanceof InvocationTargetException ite && ite.getTargetException() != null) {
				String msg = ite.getTargetException().getMessage();
				skip = msg != null && (msg.contains("duplicate") || msg.contains("slice"));
			}
			if (!skip) {
				log.warn("Can't release direct buffer", e);
			}
        }

	}

	public static int min(long a1, long a2) {
		return (int) (a1 < a2 ? a1 : a2);
	}
}
