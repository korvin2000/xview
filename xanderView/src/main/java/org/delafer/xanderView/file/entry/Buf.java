package org.delafer.xanderView.file.entry;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Buf implements Closeable {

	private static final Logger log = LoggerFactory.getLogger(Buf.class);

	private transient ByteBuffer b;
	/** releases b's memory: a mapped or native region via its FFM arena ({@link HelperFS#arenaBuf}); null for heap memory */
	private transient Closeable c;

	Buf(final ByteBuffer bb) {
		this.b = bb;
	}

	Buf(final ByteBuffer bb,final Closeable c) {
		this.b = bb;
		this.c = c;
	}

	public final ByteBuffer get() {
		b.rewind();
		return b;
	}

	public final byte[] getArray(boolean close) {
		 b.rewind();
		 final byte[] bytes = new byte[b.remaining()];
	     b.get(bytes);
	     b.rewind();

	     if (close)
			try {
				this.close();
			} catch (IOException e) {
				log.warn("Can't close buffer", e);
			}

		 return bytes;
	}

	public void close() throws IOException {
		final Closeable owner = c;
		c = null; // idempotent: an arena must be closed exactly once
		if (null != owner) {
			owner.close();
		}
		//b = null;
	}

}
