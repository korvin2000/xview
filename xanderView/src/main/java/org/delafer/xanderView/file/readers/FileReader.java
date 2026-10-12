package org.delafer.xanderView.file.readers;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.delafer.xanderView.common.Selector;
import org.delafer.xanderView.comparator.BasicFileComparator;
import org.delafer.xanderView.comparator.ComparatorFactory;
import org.delafer.xanderView.file.ContentChangeWatcher;
import org.delafer.xanderView.file.entry.HelperFS;
import org.delafer.xanderView.file.entry.ImageFS;
import org.delafer.xanderView.file.entry.ImageAbstract;
import org.delafer.xanderView.file.entry.ImageAbstract.ImageType;
import org.delafer.xanderView.gui.config.ApplConfiguration;
import org.delafer.xanderView.interfaces.IAbstractReader;

import com.sun.nio.file.ExtendedWatchEventModifier;

import net.j7.commons.base.Equals;
import net.j7.commons.io.AbstractFileProcessor;
import net.j7.commons.io.AbstractFileProcessor.FileInfo;
import net.j7.commons.io.AbstractFileProcessor.Recurse;
import net.j7.commons.io.FileUtils;

public class FileReader implements IAbstractReader {

	ContentChangeWatcher watcher;
	private WatchService watchService;
	private volatile Thread watchThread;
	private volatile boolean closing;
	private File sourceFile;
	private Recurse mode;
	private List<SevenZipReader> subReaders;

	public FileReader(File sourceFile, Recurse mode) {
		super();
		this.sourceFile = sourceFile;
		this.mode = mode;
		this.subReaders = Collections.synchronizedList(new LinkedList<>());
		this.closing = false;
	}

	public void read(final List<ImageAbstract<?>> entries) {
		try {

			AbstractFileProcessor scanner = new AbstractFileProcessor(getContainerPath()) {

				private ArrayList<FileInfo> buffer = new ArrayList<>();
				private Path last;
				public boolean accept(FileInfo fileInfo, boolean isArc, String ext) {
					//System.out.println("0:::::"+fileInfo.getPath().toString());
					if (isArc && Recurse.Recursiv.equals(FileReader.this.mode)) return true;
					ImageType imageType = ImageAbstract.getTypeByExt(ext);
					if (ImageType.UNKNOWN.equals(imageType)) return false;
					if (ImageType.ENCRYPTED.equals(imageType) && !ApplConfiguration.instance().hasPwd()) return false;
					return true;
				}

				@Override
				public void processFile(Path file, FileInfo fileInfo, Path parent) throws Exception {
					String ext = fileInfo.getExtension();
					boolean isArc = ArcTypes.isArcType(ext);
					if (!accept(fileInfo, isArc, ext)) return;
					if (isArc) {
						if (Recurse.Recursiv.equals(FileReader.this.mode)) {
							SevenZipReader szr = new SevenZipReader(file.toFile());
							szr.read(entries);
							FileReader.this.subReaders.add(szr);
						}
					} else {
						if (last != parent) {
							flushFiles();
							last = parent;
						}
						//System.out.println(fileInfo.getFileName());
						buffer.add(fileInfo);
						//ImageAbstract<?> entry = ImageFS.getInstance(FileReader.this, fileInfo.getNameWithPath(), fileInfo.getFileName(), fileInfo.getFileSize());
						//entries.add(entry);
					}
				}

				void addImage(FileInfo fileInfo) throws IOException {
					ImageAbstract<?> entry = ImageFS.getInstance(FileReader.this, fileInfo.getNameWithPath(), fileInfo.getFileName(), fileInfo.getFileSize());
					entries.add(entry);
				}

				private void flushFiles() throws IOException {
					if (buffer.isEmpty()) return;
					int numberOfItems = buffer.size();
					int desired = Selector.getElements(ApplConfiguration.getFilterCount(), numberOfItems);
					if (desired == 0) {
						for (int i = 0; i < numberOfItems; i++) {
							FileInfo entry = buffer.get(i);
							addImage(entry);
						}
					} else {
						Selector sel = new Selector(numberOfItems, desired);
						int added = 0;
						int next;
						while (-1 != (next = sel.next()) && added < desired) {
							FileInfo entry = buffer.get(next);
							added++;
							addImage(entry);
						}
					}
					buffer.clear();
				}

				public void onFinish() {
					try {
						flushFiles();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				public boolean isVirtualDirectory(String path) {
					return HelperFS.isVirtualDirectory(path);
				}
			};
			scanner.setMode(mode);
			scanner.start();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void close() {
		closing = true;
		Thread wt = watchThread;
		if (wt != null && wt != Thread.currentThread()) {
			wt.interrupt();
		}

		WatchService ws = watchService;
		if (ws != null) {
			try {
				ws.close();
			} catch (IOException | ClosedWatchServiceException ignore) {
				// Closed during termination or path switch.
			}
		}

		synchronized (subReaders) {
			for (SevenZipReader subReader : subReaders) {
				try {
					subReader.close();
				} catch (Throwable ignore) {
				}
			}
			subReaders.clear();
		}
	}

	@Override
	public void register(ContentChangeWatcher watcher) {
		this.watcher = watcher;

	}

	@Override
	public void initialize() {

		Thread t = new Thread("FileReaderWatch-" + Integer.toHexString(System.identityHashCode(this))) {

			@Override
			public void run() {
				WatchService localWatchService = null;
				try {
					Path path = Paths.get(getContainerPath());
					if (!Files.isDirectory(path)) return ;
					localWatchService = path.getFileSystem().newWatchService();
					watchService = localWatchService;

					path.register(localWatchService,new Kind[] {ENTRY_CREATE,ENTRY_DELETE}, ExtendedWatchEventModifier.FILE_TREE);
					WatchKey watchKey;
					while(!closing && !Thread.currentThread().isInterrupted()){
						try{
							watchKey = localWatchService.poll(1,TimeUnit.SECONDS);
							if (watchKey == null) continue;
							List<WatchEvent<?>> events = watchKey.pollEvents();
							for(WatchEvent<?> event : events){
								Kind<?> kind = event.kind();
			                    if (kind == StandardWatchEventKinds.OVERFLOW) {
			                        continue;
			                    }
//								Path watchedPath = (Path) watchKey.watchable();
								Path target = (Path)event.context();
								Path fullpath = path.resolve(target);
								if (null != watcher) {
									try {
										watcher.onEvent(asEnum(kind), fullpath.toString());
									} catch (IOException io) {
										if (!closing) throw io;
									}
								}
							}
							if(!watchKey.reset()){
								break;
							}
						}catch(InterruptedException e){
							Thread.currentThread().interrupt();
							break;
						} catch (ClosedWatchServiceException closed) {
							break;
						}
					}
				} catch (IOException e) {
					if (!closing) {
						e.printStackTrace();
					}
				} finally {
					watchThread = null;
					if (watchService == localWatchService) {
						watchService = null;
					}
					if (localWatchService != null) {
						try {
							localWatchService.close();
						} catch (IOException | ClosedWatchServiceException ignore) {
						}
					}
				}
			}

		};
		t.setDaemon(true);
		watchThread = t;
		t.start();



	}

	private FileEvent asEnum(Kind<?> kind) {
		if (StandardWatchEventKinds.ENTRY_CREATE == kind) return FileEvent.Create;
		if (StandardWatchEventKinds.ENTRY_DELETE == kind) return FileEvent.Delete;
		if (StandardWatchEventKinds.ENTRY_MODIFY == kind) return FileEvent.Modify;
		return FileEvent.Other;
	}

//	public static void main(String[] args) {
//		FileReader r = new FileReader();
//		r.initialize("C:\\work\\cc\\");
//	}
	
	@SuppressWarnings("unchecked")
	public ImageAbstract<?> getEntryByIdentifier(Object id) throws IOException {
		String filePath = (String) id;

		FileInfo fileInfo = new FileInfo(Path.of(filePath));
		ImageAbstract<?> entry = ImageFS.getInstance(this, fileInfo.getNameWithPath(), fileInfo.getFileName(), fileInfo.getFileSize());
		return entry;
	}

	@Override
	public Comparator<ImageAbstract<?>> getComparator() {
		return ComparatorFactory.getComparator();
	}

	@Override
	public String getContainerPath() {
		File aFile = sourceFile;
		boolean isDir = HelperFS.isAnyDirectory(aFile);
		if (!isDir) {
			aFile = aFile.getParentFile();
		}

		return isDir && aFile.exists() ? FileUtils.extractFullPathName(aFile) : "";
	}

	@Override
	public Object getSingleEntry() {
		return HelperFS.isAnyFile(sourceFile) ? FileUtils.extractFullPathName(sourceFile) : null ;
	}

//	public static void main(String[] args) {
//		FileReader r = new FileReader();
//		System.out.println("x"+r.getContainerPath(new File("C:\\work\\workspaces\\abovo\\abovo.7z")));
//	}

}
