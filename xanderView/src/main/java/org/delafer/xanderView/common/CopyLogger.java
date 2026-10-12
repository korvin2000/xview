package org.delafer.xanderView.common;
import org.delafer.xanderView.file.entry.ImageAbstract;
import org.delafer.xanderView.gui.config.ApplConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Objects;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
/**
 * Alex, here is a performance-oriented FIFO text buffer class with:
 * - Fixed capacity buffer
 * - Auto-flush to file on overflow
 * - Explicit flush method for external calls
 * - Efficient memory usage
 *
 * The code focuses on speed, memory efficiency and simplicity:
 * - Uses ArrayDeque for O(1) enqueue/dequeue operations.
 * - Minimizes locking scope and avoids unnecessary object creation.
 * - Uses NIO.2 with BufferedWriter for efficient writes.
 */
public final class CopyLogger {
	private final Path outputFile;
	private final int capacity;
	private final ArrayDeque<String> buffer;
	private final Object lock = new Object();

	private static CopyLogger instance;

	private static final String PREFIX = "history_";
	private static final String EXTENSION = ".log";
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd_MM_yy");

	public static String generateFileName() {
		String timeSuffix = LocalDateTime.now().format(TIME_FORMATTER);
		return PREFIX + timeSuffix + EXTENSION;
	}

	public synchronized static CopyLogger getInstance() {
		if (instance == null) {
			ApplConfiguration cfg = ApplConfiguration.instance();
			Path fileName = Paths.get(cfg.getCopyDir(), generateFileName());
			instance = new CopyLogger(fileName);
		}
		return instance;
	}

	public CopyLogger(Path fileName) {
		this(fileName, 5);
	}

	public CopyLogger(Path outputFile, int capacity) {
		this.outputFile = Objects.requireNonNull(outputFile, "outputFile");
		if (capacity <= 0) throw new IllegalArgumentException("Capacity must be > 0");
		this.capacity = capacity;
		this.buffer = new ArrayDeque<>(capacity);
	}

	public void add(ImageAbstract<?> entry, File target) throws IOException {
		String name = '"' + target.getName() + "\" <- "+entry.getSourcePath();
		this.add(name);
	}

	/**
	 * Adds a line to the FIFO buffer.
	 * If the buffer reaches full capacity, it flushes to disk.
	 */
	public void add(String line) throws IOException {
		Objects.requireNonNull(line, "line");
		synchronized (lock) {
			buffer.addLast(line);
			if (buffer.size() >= capacity) {
				flushLocked(0);
			}
		}
	}

	/**
	 * Flushes the buffer contents to file. Can be called from another thread.
	 */
	public void flush() throws IOException {
		synchronized (lock) {
			if (!buffer.isEmpty()) {
				flushLocked(0);
			}
		}
	}

	public static void flushIfNeeded() {
		if (instance != null) {
			try {
				instance.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void flushLocked(int tryNo) throws IOException {
		if (tryNo > 2) return ;
		// Buffer must not be empty here. Append all lines and clear.
		try (BufferedWriter writer = Files.newBufferedWriter(outputFile,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE)) {
			while (!buffer.isEmpty()) {
				writer.write(buffer.removeFirst());
				writer.newLine();
			}
		} catch (IOException e) {
			try { Thread.sleep(337); } catch (InterruptedException ignore) {}
			if (!buffer.isEmpty())
				flushLocked(tryNo+1);
			else
				throw e;
		}
	}
}
