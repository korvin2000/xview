package org.delafer.xanderView.file.entry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import net.j7.commons.base.Equals;
import net.j7.commons.io.FileUtils;
import net.j7.commons.streams.Streams;
import net.j7.commons.strings.StringUtils;

import net.sf.sevenzipjbinding.SevenZipException;
import org.delafer.xanderView.common.ImageSize;
import org.delafer.xanderView.gui.config.ApplInstance;
import org.delafer.xanderView.hash.Hasher;
import org.delafer.xanderView.interfaces.IAbstractReader;
import org.delafer.xanderView.interfaces.IImageEntry;

public abstract class ImageAbstract<E> implements IImageEntry<E> {

	public enum ImageType{JPEG, BMP, PNG, AVIF, WEBP, JXL, AIM, ENCRYPTED, XAV, UNKNOWN};

	protected String name;

	protected long size;
	protected Long crc;

	protected ImageSize imageSize;
	protected ImageType imageType;

	protected IAbstractReader parent;

	protected E identifier;

	public static ImageType getType(String name) {
		return getTypeByExt(FileUtils.getExtension(name));
	}

	public static ImageType getTypeByExt(String ext) {
		ext = ext.trim().toLowerCase();
		ImageType tmp = types.get(ext);
		return tmp != null ? tmp : ImageType.UNKNOWN;
	}

	public Long CRC() {
		return crc;
	}

	/**
	 * @param crc the crc to set
	 */
	public void setCRC(Long crc) {
		this.crc = crc;
	}


	public String name() {
		return name;
	}

	protected static String shorten(String name) {
		if (StringUtils.isEmpty(name)) return name;
		if (name.length() <= 50) return name;
		StringBuilder sb = new StringBuilder();
		sb.append(name.subSequence(0, 24));
		sb.append("~");
		sb.append(name.subSequence(name.length()-24, name.length()));
		return sb.toString();
	}

	public String shortName() {
		return shorten(name());
	}

	public long size() {
		return size;
	}

	public abstract long lastModified() throws IOException;

	public E getIdentifier() {
		return identifier;
	}

	public ImageSize getImageSize() {
		return imageSize;
	}


	public ImageType getImageType() {
		if (null == this.imageType) {
			this.imageType = getType(name);
		}
		return imageType;
	}

	protected void calcCRC(ByteBuffer ret) {
		if (null == crc) {
			this.crc = Hasher.hash().calc(ret, this.size);
		}
	}

	protected void calcSafeCRC(Buf ret) {
		if (null == crc) {
			this.crc = Hasher.hash().calc(ret.get(), this.size);
			Streams.closeSilently(ret);
		}
	}

	protected abstract Buf rawData(E identifier, int size) throws IOException;

	protected abstract String lastEntryId(ImageAbstract<E> ia);

	public Buf content() {
		ApplInstance.lastEntry = lastEntryId(this);
		try {
			final Buf ret = rawData(identifier, X);
			calcCRC(ret.get());
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int hashCode() {
		int result = (int) (size ^ (size >>> 32));
		return result;
	}

	public boolean isEquals(ImageAbstract<?> thisObj, Object obj) {
		if (thisObj == obj) return true;
		if (!(obj instanceof ImageAbstract))  return false;
		final ImageAbstract<?> o = (ImageAbstract<?>) obj;

		if (thisObj.size != o.size) return false;
		
		final Long tcrc = thisObj.crc, ocrc = o.crc;
		if (tcrc != null && ocrc != null) {
			return Objects.equals(tcrc, ocrc);
		}
		
		if (!Equals.equal(thisObj.name, o.name)) return false;

		return true;
	}
	
	public abstract ImageAbstract<?> cloneObj();
	

	@Override
	public boolean equals(Object obj) {
		return isEquals(this, obj);
	}
	
	public String toString() {
		return String.format("ImageAbstract [name=%s, imageType=%s, size=%s, crc=%s]", name, imageType, size, crc);
	}

	public abstract String getSourcePath();
}
