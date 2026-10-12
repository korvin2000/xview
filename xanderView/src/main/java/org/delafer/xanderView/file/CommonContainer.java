package org.delafer.xanderView.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import net.j7.commons.io.FileUtils;
import org.delafer.xanderView.comparator.RandomComparator;
import org.delafer.xanderView.file.entry.ImageAbstract;
import org.delafer.xanderView.file.entry.ImageAbstract.ImageType;
import org.delafer.xanderView.file.readers.ArcTypes;
import org.delafer.xanderView.file.readers.FileReader;
import org.delafer.xanderView.file.readers.SevenZipReader;
import org.delafer.xanderView.gui.config.ApplConfiguration;
import org.delafer.xanderView.gui.config.ApplInstance;
import org.delafer.xanderView.interfaces.IAbstractReader;
import org.delafer.xanderView.interfaces.IAbstractReader.FileEvent;
import org.delafer.xanderView.sound.SoundBeep;

import net.j7.commons.base.Equals;
import net.j7.commons.base.Numbers;
import net.j7.commons.collections.SortedLinkedList;
import net.j7.commons.io.AbstractFileProcessor.Recurse;
import net.j7.commons.io.TextFileUtils;
import net.j7.commons.io.TextFileUtils.TextReader;
import net.j7.commons.strings.StringUtils;

public class CommonContainer {

	SortedLinkedList<ImageAbstract<?>> images;
	ListIterator<ImageAbstract<?>> iterator;
	File pathFile;
//	String pathContainer;
	IAbstractReader reader;
	ImageAbstract<?> current = null;
	int direction = 0;
	boolean loop;


	public int size() {
		return images.size();
	}

	public CommonContainer(String locationArg) {
		super();

		Object entry = null;
		if (StringUtils.isEmpty(locationArg)) {
			File tryIt = new File(ApplInstance.LAST_ENTRY);
			if (tryIt.exists()) {
				try {
					TextReader tr = TextFileUtils.createTextReader(tryIt);
					String content = tr.readAll();
					String[] cont = content.trim().split(ApplInstance.LAST_ENTRY_DIV);
					locationArg = cont.length>0 ? cont[0] : null;

					if (locationArg != null && locationArg.length() >= 2) {
					if (isLetter(locationArg.charAt(0))
						&& ':' == locationArg.charAt(1)) locationArg = locationArg.substring(2);
				}

					entry = cont.length>1 ? Numbers.getResultAsInteger(cont[1]) : null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}


		this.pathFile = new File(locationArg != null ? locationArg : currentPath());
		this.reader = getReader(pathFile);
//		this.pathContainer = reader.getContainerPath();
		Comparator<ImageAbstract<?>> comparator = this.reader.getComparator();
		this.images = new SortedLinkedList<ImageAbstract<?>>(comparator);
		loop = ApplConfiguration.instance().getBoolean(ApplConfiguration.LOOP_CURRENT_SOURCE);
		initialize();

		if (null == entry)
		entry = reader.getSingleEntry();

		if (entry == null) {
			if (images.size()>0) {
				getNext();
			}
		} else {
			try {
				current = reader.getEntryByIdentifier(entry);
				updateIterator();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	protected void initImagesForComparator(Comparator<ImageAbstract<?>> comparator) {
		if (null == comparator) {
			comparator = this.reader.getComparator();
		}
		this.images = new SortedLinkedList<ImageAbstract<?>>(this.images, comparator);
		if (comparator instanceof RandomComparator) {
			Collections.shuffle(this.images, RandomComparator.getStrongRandom());
		}
		updateIterator();
	}
	
	private static String currentPath() {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		return s;
	}

	private boolean isLetter(char ch) {
		return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
	}

	private IAbstractReader getReader(File aFile) {
		boolean isFile = aFile.isFile();

		if (isFile) {
			String name = aFile.getName().toLowerCase();
			String ext = FileUtils.getExtension(name);
			if (ArcTypes.isArcType(ext)) {
				return new SevenZipReader(aFile);
			}

		}
		Recurse mode = ApplConfiguration.instance().isRecursive() ? Recurse.Recursiv : Recurse.FNE;
		return new FileReader(aFile, mode);
	}

	public void initialize() {

		try {
			reader.register(new ContentChangeWatcher() {

				@Override
				public void onEvent(FileEvent type, Object id)throws IOException {
					switch (type) {
					case Create:
						String fileName = id.toString();
						File aFile = new File(fileName);
						if (aFile.isDirectory()) return ;

						ImageType imgType = ImageAbstract.getType(fileName);
						if (ImageType.UNKNOWN.equals(imgType)) return ;

						ImageAbstract<?> entryNew = reader.getEntryByIdentifier(id);
						images.add(entryNew);
						updateIterator();
						break;
					case Delete:
						ImageAbstract<?> entryDel = findByIdentifier(""+id);
						images.remove(entryDel);
						if (current != null) {
							if (entryDel != null && Equals.equal(current.getIdentifier(), entryDel.getIdentifier())) {
								current = null;
							}
						}
						updateIterator();
						break;
					default:
					}

				}

			});

			reader.initialize();
			readStructure(images);
			iterator = images.listIterator();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected ImageAbstract<?>  findByIdentifier(String ident) {
		for (ImageAbstract<?> next : images) {
			if (Equals.equal(next.getIdentifier(), ident)) return next;
		}
		return null;

	}

	protected void readStructure(SortedLinkedList<ImageAbstract<?>> list) throws Exception {
		reader.read(list);
	}

	public int currentIndex() {
		int idx = this.getNextIndex();
		if (direction == -1) idx++;
		return idx;
	}

	public ImageAbstract<?> getNext() {
		if (direction == -1) {
			if (iterator.hasNext()) {
				iterator.next();
				direction = 0;
			}
		}
		if (iterator.hasNext()) {
			current = iterator.next();
			direction = 1;
		} else {
			if (loop) {
				gotoFirst();
			}
			//current = null;
		}

		return current;
	}

	private void gotoLast() {
		while (iterator.hasNext()) {
			current = iterator.next();
		}
		beep();
		direction = 1;
	}

	private void beep() {
		SoundBeep.beep();

	}

	private void gotoFirst() {
		while (iterator.hasPrevious()) {
			current = iterator.previous();
		}
		beep();
		direction = -1;
	}

	public int getNextIndex() {
		return iterator.nextIndex();
	}

	public ImageAbstract<?> getPrevious10() {
		if (size()>= 10) for (int i = 8; i >= 0; i--) getPrevious();

		return getPrevious();
	}

	public ImageAbstract<?> getNext10() {
		if (size()>= 10) for (int i = 8; i >= 0; i--) getNext();

		return getNext();
	}

	public ImageAbstract<?> getPrevious() {
		if (direction == 1) {
			if (iterator.hasPrevious()) {
				iterator.previous();
				direction = 0;
			}
		}
		if (iterator.hasPrevious()) {
			current = iterator.previous();
			direction = -1;
		} else {
			if (loop) {
				gotoLast();
			}
			//current = null;
		}

		return current;
	}

	public int getPreviousIndex() {
		return iterator.previousIndex();
	}

	public void onContainerContentChange() {

	}

	private void updateIterator() {
		iterator = current != null ? images.listIterator(current) : images.listIterator();
		if (iterator.hasNext()) {
			iterator.next();
			direction = 1;
		} else {
			direction = 0;
		}
	}


	public ImageAbstract<?> getCurrent() {
		return current;
	}

	public String getLocation() {

		return pathFile != null ? pathFile.getParent() : ".";
	}

}
