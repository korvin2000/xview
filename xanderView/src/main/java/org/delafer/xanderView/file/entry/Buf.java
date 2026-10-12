package org.delafer.xanderView.file.entry;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class Buf implements Closeable {

	private transient ByteBuffer b, owner;
	private transient Closeable c;

	Buf(final ByteBuffer bb) {
		this.b = bb;
	}

	Buf(final ByteBuffer bb, final ByteBuffer owner) {
		this.b = bb;
		this.owner = owner;
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
				e.printStackTrace();
			}

		 return bytes;
	}

	public void close() throws IOException {
		HelperFS.closeDirectBuffer(null == owner ? b : owner);
		if (null != c) {
			c.close();
			c = null;
		}
		//b = null;
	}

}
