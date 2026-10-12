/**
 *
 */
package org.delafer.xanderView.file.entry;

import java.io.IOException;

import net.j7.commons.io.FileUtils;

import org.delafer.xanderView.file.entry.Encryptor.DecData;
import org.delafer.xanderView.gui.config.ApplConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tavrovsa
 *
 */
public class ImageEnc<E> extends ImageAbstract<E> {

	private static final Logger log = LoggerFactory.getLogger(ImageEnc.class);

	ImageAbstract<E> si;
	Buf buf;
	
	public ImageEnc(ImageAbstract<E> si) {
		this.si = si;
		this.parent = si.parent;
		this.crc = si.crc;
		this.identifier = si.identifier;
		this.size = si.size();
		this.name = EncodeName.encrypt(FileUtils.getBaseName(si.name()))+".cry";
		init(si);
	}

	/**
	 * @param si
	 */
	private void init(ImageAbstract<E> si) {
		try {

		} catch (Exception e) {
			log.error("Can't initialize encrypted image", e);
		}
	}



	/* (non-Javadoc)
	 * @see org.delafer.xanderView.file.entry.ImageAbstract#getIdentifier()
	 */
	@Override
	public E getIdentifier() {
		return si.getIdentifier();
	}


	@Override
	protected Buf rawData(E identifier, int size) throws IOException {
		DecData decData = new DecData(si.rawData(identifier, size), si.name(), (int)si.size());
		Buf buf = new Buf( Encryptor.encrypt(decData) );
		decData.close();
		return buf;
	}

	@Override
	public long size() {
		return si.size();
	}

	public long lastModified() throws IOException {
		return this.si != null ? this.si.lastModified() : 0;
	}

	@Override
	protected String lastEntryId(ImageAbstract<E> ia) {
		return si.lastEntryId(si);
	}
	
	public boolean equals(Object obj) {
		return si.isEquals(this, obj);
	}

	public static ImageAbstract<?> getEncrypted(ImageAbstract<?> inp) {
		if (null == inp || inp instanceof ImageEnc<?>) {
			return inp;
		}
		
		if (inp instanceof ImageDec<?>) {
			ImageDec<?> id = (ImageDec<?>) inp;
			return id.getEncrypted();
		}
		
		return ApplConfiguration.instance().hasPwd() ? new ImageEnc<>(inp) : inp;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ImageAbstract<E> cloneObj() {
		ImageEnc<E> cloned = new ImageEnc<E>(si != null ? (ImageAbstract<E>)si.cloneObj() : null);
		cloned.buf = this.buf;
		cloned.parent = this.parent;
		cloned.crc = this.crc;
		cloned.size = this.size;
		cloned.identifier = this.identifier;
		cloned.name = this.name;
		cloned.imageType = this.imageType;
		return cloned;
	}

	public String getSourcePath() {
		return this.si.getSourcePath();
	}

}
