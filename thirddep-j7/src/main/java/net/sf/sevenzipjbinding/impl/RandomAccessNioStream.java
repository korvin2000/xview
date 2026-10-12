package net.sf.sevenzipjbinding.impl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.SevenZipException;

import com.sun.nio.file.ExtendedOpenOption;

/**
 * Implementation of {@link IInStream} using {@link RandomAccessFile}.
 *
 * @author Alan Toy
 * @version 1.0.0
 */
public final class RandomAccessNioStream implements IInStream {
	private final transient SeekableByteChannel  randomAccessFile;

	/**
	 * Constructs instance of the class from path name.
	 *
	 * @param randomAccessFile
	 *            random access file to use
	 */
	public RandomAccessNioStream(String location) {
		SeekableByteChannel  raf = null;
		try {
			raf = Files.newByteChannel(Paths.get(location), StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS,  ExtendedOpenOption.NOSHARE_WRITE);
		} catch (IOException e) {
			throw new IllegalArgumentException(String.format("Error opening: %s",location));
		}
		randomAccessFile = raf;
	}

	/**
	 * {@inheritDoc}
	 */
	public final long seek(long offset, int seekOrigin) throws SevenZipException {
		try {
			switch (seekOrigin) {
			case SEEK_SET:
				randomAccessFile.position(offset);
				break;

			case SEEK_CUR:
				offset += randomAccessFile.position();
				randomAccessFile.position(offset);
				break;

			case SEEK_END:
				offset += randomAccessFile.size();
				randomAccessFile.position(offset);
				break;

			default:
				throw new RuntimeException("Seek: unknown origin: " + seekOrigin);
			}

			return randomAccessFile.position();
		} catch (IOException e) {
			throw new SevenZipException("Error while seek operation", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final int read(byte[] data) throws SevenZipException {
		try {
			int read = randomAccessFile.read(ByteBuffer.wrap(data));
			return read >= 0 ? read : 0;
		} catch (IOException e) {
			throw new SevenZipException("Error reading random access file", e);
		}
	}

	/**
	 * Closes random access file. After this call no more methods should be called.
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		randomAccessFile.close();
	}
}

