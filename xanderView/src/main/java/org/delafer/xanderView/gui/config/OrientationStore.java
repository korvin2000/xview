package org.delafer.xanderView.gui.config;


import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import net.j7.commons.io.FilePath;
import net.j7.commons.io.FilePath.PathType;
import net.j7.commons.types.BitNumberRange;
import net.j7.commons.types.Range;

import org.delafer.xanderView.orientation.OrientationCommons.Orientation;

import com.carrotsearch.hppc.LongLongHashMap;
import com.carrotsearch.hppc.procedures.LongLongProcedure;


public class OrientationStore {


	private String fileName;
	private LongLongHashMap map;
	private boolean dirty;
	//vsego 64
	private final static Range rangeOrient = Range.range(0, 4); // 0 - 15
	private final static Range scaleOrient = Range.range(4, 6); // 0 - 63

	private final static Range hrOffset = Range.range(10, 6); // 0 - 63
	private final static Range vrOffset = Range.range(16, 6); // 0 - 63
	private final static Range gammaRange = Range.range(22, 6); // 0 - 63

	/**
	 * Lazy-loaded Singleton, by Bill Pugh *.
	 */
	private static final class Holder {
		/** The Constant INSTANCE. */
		private final static transient OrientationStore INSTANCE = new OrientationStore();
	}

	/**
	 * Gets the single instance of ResourcesDR.
	 *
	 * @return single instance of ResourcesDR
	 */
	public static final OrientationStore instance() {
		return Holder.INSTANCE;
	}

	private OrientationStore() {
		map  = new LongLongHashMap();
		dirty =false;
		init();
	}

	private void init() {
		fileName = FilePath.as().dir(PathType.ApplicationData).dir(ApplConfiguration.XANDER_VIEW).file("imagedata.bin").forceExists().build();
	}

	public void load() throws IOException {
		try (DataInputStream dos = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(fileName))))) {
			while (true) {
				long key = dos.readLong();
				long value = dos.readLong();
				map.put(key, value);
			}
		} catch (EOFException e) {
			//reached end of the file
		}
	}

	public void save() throws IOException {

		if (!dirty) return ;

		dirty = false;

		Path target = Path.of(fileName);
		Path tmp = Files.createTempFile(target.getParent(), "imagedata", ".tmp");
		try {
			try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tmp.toFile())))) {
				map.forEach(new LongLongProcedure() {
					@Override
					public void apply(long key, long value)  {
						try {
							dos.writeLong(key);
							dos.writeLong(value);
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					}
				});
				dos.flush();
			}
			try {
				Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException ignore) {
				Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(tmp);
		}

	}

	private int enumOrderNum(Enum<?> enumVal) {
		return enumVal != null ? enumVal.ordinal()+1 : 0;
	}

	private Orientation enumByNum(int num) {
		if (num == 0) return null;
		return Orientation.values()[--num];
	}

	public ImageData getImageData(long crc) {
		return new ImageData(crc);
	}

	public void setOrientation(long crc, Orientation orient) {
		long valueOld = map.get(crc);
		long valueNew = BitNumberRange.set(valueOld, rangeOrient,  enumOrderNum(orient));
		if (valueOld == valueNew) return ;
		if (valueNew != 0l) {
			map.put(crc, valueNew);
		} else {
			map.remove(crc);
		}
		dirty =true;
	}

	private static int toPBV(int v, int maxVal) {
		if (v != 0) {
			v -= maxVal;
			if (v>=0) v++;
		}
		return v;
	}

	private static int fromPBV(int v, int maxVal) {
		if (v != 0) {
			if (v > 0) v--;
			v += maxVal;
		}
		return v;
	}

	private void setByType(long crc, int value, Range range, int maxVal) {
		long valueOld = map.get(crc);
		long valueNew = BitNumberRange.set(valueOld, range,  fromPBV(value, maxVal));
		if (valueOld == valueNew) return ;
		if (valueNew != 0l) {
			map.put(crc, valueNew);
		} else {
			map.remove(crc);
		}
		dirty =true;
	}

	public void setScaleConst(long crc, int scale) {
		setByType(crc, scale, scaleOrient, 32);
	}

	public void setGammaConst(long crc, int gamma) {
		setByType(crc, gamma, gammaRange, 32);
	}

	public void setHrOffset(long crc, int offset) {
		setByType(crc, offset, hrOffset, 32);
	}

	public void setVrOffset(long crc, int offset) {
		setByType(crc, offset, vrOffset, 32);
	}


//	public static void main(String[] args) {
//		OrientationStore os = OrientationStore.instance();
//
//
////		for (int i = 0; i < 30000; i++) {
////	          long crc = RandomUtil.getRandom().nextLong();
////	          int orient = RandomUtil.getRandomInt(0, 7);
////			os.setOrientation(crc, Orientation.values()[orient]);
////		}
//
//		try {
//			os.load();
//
//			OrientationStore.instance().map.forEach(new LongLongProcedure() {
//
//				@Override
//				public void apply(long key, long value)  {
//					try {
//						System.out.println(key+" : "+value);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//
//
//				}
//			});
//			System.out.println("done");
//			System.out.println(OrientationStore.instance().map.size());
//			System.out.println(OrientationStore.instance().getOrientation(1062222676426126259l));
//			System.out.println(OrientationStore.instance().getOrientation(2313283709819813184l));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}

	public class ImageData implements Serializable {

		private static final long serialVersionUID = 123456L;

		long bitmask;

		public ImageData(long crc) {
			this.bitmask = map.get(crc);
		}

		public Orientation getOrientation() {
			long value = BitNumberRange.value(bitmask, rangeOrient);
			Orientation orient = enumByNum((int)value);
			return orient;
		}

		public int getScaleConst() {
			long value = BitNumberRange.value(bitmask, scaleOrient);

			return toPBV((int)value, 32);//32 = 64/2
		}

		public int getHrOffset() {
			long value = BitNumberRange.value(bitmask, hrOffset);
			return toPBV((int)value, 32);//32 = 64/2
		}

		public int getVrOffset() {
			long value = BitNumberRange.value(bitmask, vrOffset);
			return toPBV((int)value, 32);//32 = 64/2
		}

		public int getGamma() {
			long value = BitNumberRange.value(bitmask, gammaRange);
			return toPBV((int)value, 32);//32 = 64/2
		}
	}

}
