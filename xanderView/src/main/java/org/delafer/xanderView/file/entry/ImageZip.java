package org.delafer.xanderView.file.entry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Objects;

import net.j7.commons.io.FileUtils;
import net.sf.sevenzipjbinding.*;

import org.delafer.xanderView.file.readers.SevenZipReader;
import org.delafer.xanderView.gui.config.ApplInstance;
import org.delafer.xanderView.interfaces.IAbstractReader;

public class ImageZip extends ImageAbstract<Integer> {

	IInArchive archive;

	public static ImageAbstract<Integer> getInstance(IAbstractReader parent, IInArchive archive, Integer id, String name, long size) {
		ImageZip fs = new ImageZip(parent, archive, id, name, size);
		if (ImageType.ENCRYPTED.equals(fs.getImageType())) {
			return new ImageDec<Integer>(fs);
		}
		return fs;
	}


	private ImageZip(IAbstractReader parent, IInArchive archive, Integer id, String name, long size) {
		this.parent = parent;
		this.archive = archive;
		this.name = name;
		this.size = size;
		this.identifier = id;
	}

	@Override
	public String shortName() {
		return shorten(FileUtils.getFileName(this.name()));
	}

	private transient long lastModified = -1;

	@Override
	public long lastModified() throws IOException {
		if (lastModified == -1) {
			Date date = (Date) archive.getProperty(this.identifier, PropID.LAST_MODIFICATION_TIME);
			this.lastModified = date != null ? date.getTime() : 0;
		}
		return lastModified;
	}

	public Buf rawData(Integer identifier, int size) throws IOException {
		if (size < 0) {
			Long longSize = (Long) archive.getProperty(this.identifier, PropID.SIZE);
			size = null != longSize ? longSize.intValue() : size;
		}
		MyExtractCallback extract = new MyExtractCallback(archive, size);
		archive.extract(new int[] {identifier}, false, extract);
		Buf ret =  new Buf(extract.data());
		return ret;
	}

	protected String lastEntryId(ImageAbstract<Integer> ia) {
		return ia.parent.getContainerPath()+ApplInstance.LAST_ENTRY_DIV+ia.identifier;
	}

	public String getSourcePath() {
		return this.parent.getContainerPath();
	}

	@Override
	public String toString() {
		return String.format("ImageZip [identifier=%s, name=%s, size=%s, crc=%s, imageType=%s]", identifier, name, size, crc, imageType);
	}
	
	@Override
	public boolean isEquals(ImageAbstract<?> thisObj, Object obj) {
		if (thisObj == obj) return true;
		if (!(obj instanceof ImageAbstract))  return false;
		final ImageAbstract<?> o = (ImageAbstract<?>) obj;

		if (thisObj.size != o.size) return false;

		final Long tcrc = thisObj.CRC(), ocrc = o.CRC();

		if (tcrc != null && ocrc != null) {
			return Objects.equals(tcrc, ocrc);
		}
		
		if (!Objects.equals(thisObj.name(), o.name())) return false;

		return true;		
	}
	
	@Override
	public boolean equals(Object obj) {
		return isEquals(this, obj);
	}
	
	@Override
	public ImageAbstract<?> cloneObj() {
		ImageZip cloned = new ImageZip(parent, archive, identifier, name, size);
		cloned.crc = this.crc;
		cloned.imageSize = this.imageSize;
		cloned.imageType = this.imageType;
		return cloned;
	}

	@SuppressWarnings("unused")
	public static class MyExtractCallback implements IArchiveExtractCallback {

		private int pointer = 0;
		private int index;
		private boolean skipExtraction;
		private IInArchive inArchive;
//		private byte[] content;
		private ByteBuffer bb;

		public MyExtractCallback(IInArchive inArchive, long size) {
			this.inArchive = inArchive;
			bb = ByteBuffer.allocateDirect((int)size);

//			content = new byte[(int)size];
		}

		public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
			this.index = index;

			skipExtraction = (Boolean) inArchive.getProperty(index, PropID.IS_FOLDER);
			if (skipExtraction || extractAskMode != ExtractAskMode.EXTRACT) return null;

			return new ISequentialOutStream() {
				public int write(byte[] data) throws SevenZipException {
//					System.arraycopy(data, 0, content, pointer, data.length);
//					pointer += data.length;

					bb.put(data);

					return data.length; // Return amount of proceed data
				}
			};
		}

		public ByteBuffer data() {
			bb.rewind();
			return bb;
		}

		public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {}

		public void setTotal(long total) throws SevenZipException {}

		public void setCompleted(long completeValue) throws SevenZipException {}

		public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {}

	}

}
