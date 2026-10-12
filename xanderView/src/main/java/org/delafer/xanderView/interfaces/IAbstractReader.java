package org.delafer.xanderView.interfaces;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.delafer.xanderView.file.ContentChangeWatcher;
import org.delafer.xanderView.file.entry.ImageAbstract;

public interface IAbstractReader {


	public enum FileEvent{Create, Delete, Modify, Other};

	public void initialize();

	public void read(List<ImageAbstract<?>> entries);

	public void close();

	public void register(ContentChangeWatcher watcher);

	public <E extends ImageAbstract<?>>E getEntryByIdentifier(Object id) throws IOException ;

	public Comparator<ImageAbstract<?>> getComparator();

	public String getContainerPath();

	public Object getSingleEntry();
}
