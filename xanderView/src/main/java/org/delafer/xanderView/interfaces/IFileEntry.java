package org.delafer.xanderView.interfaces;

import org.delafer.xanderView.file.entry.Buf;

public interface IFileEntry<E> {


	public E getIdentifier();

	public Long CRC();

	public String name();

	public long size();

	public Buf content();

}
