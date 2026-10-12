package org.delafer.xanderView.hash;

import java.nio.ByteBuffer;

import net.jpountz.xxhash.XXHash64;
import net.jpountz.xxhash.XXHashFactory;

public class Hasher {

	private XXHash64 hash64;
	/**
	 * Number of leading file bytes that feed the content hash. A deliberate
	 * speed-over-completeness trade-off: only this prefix is read, so two files sharing
	 * the same size and the same first {@code HSIZE} bytes hash equal (and are treated as
	 * duplicates). A larger value lowers that collision chance at the cost of a slightly
	 * bigger read.
	 * <p>
	 * NOTE: this value is baked into every persisted CRC (the OrientationStore /
	 * {@code imagedata.bin} keys). Changing it re-keys those entries, so per-image view
	 * state saved by an earlier value will no longer be found.
	 */
	public static final int HSIZE = 3072;


	/**
	 * Lazy-loaded Singleton, by Bill Pugh *.
	 */
	private static final class Holder {
		/** The Constant INSTANCE. */
		private final static transient Hasher INST = new Hasher();
	}

	/**
	 * Gets the single instance of ResourcesDR.
	 *
	 * @return single instance of ResourcesDR
	 */
	public final static Hasher hash() {
		return Holder.INST;
	}


	private Hasher() {
		XXHashFactory hash = XXHashFactory.fastestInstance();
		hash64 = hash.hash64();
	}
	public long calc(ByteBuffer buf, long seed) {
		// Only the first HSIZE bytes contribute to the hash, so read exactly those -
		// straight from the buffer (memory-mapped / direct memory included), with no
		// full-file allocation or copy. A duplicate is used so the caller's position,
		// limit and byte order stay untouched. xxHash64 produces the same value as the
		// byte[] path, so CRCs computed by earlier versions remain stable.
		final ByteBuffer view = buf.duplicate();
		view.clear();
		final int len = min(view.remaining(), HSIZE);
		return hash64.hash(view, 0, len, seed);
	}


	public long calc(byte[] buf, long seed) {
		return hash64.hash(buf, 0, min(buf.length, HSIZE), seed);
	}


	private int min(int a1, int a2) {
		return a1 < a2 ? a1 : a2;
	}

}
