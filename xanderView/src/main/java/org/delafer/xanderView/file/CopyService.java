package org.delafer.xanderView.file;

import net.j7.commons.base.Equals;
import net.j7.commons.io.AbstractFileProcessor.Recurse;
import net.j7.commons.io.FilePath;
import net.j7.commons.io.FileUtils;
import net.j7.commons.streams.Streams;
import org.delafer.xanderView.common.*;
import org.delafer.xanderView.file.entry.*;
import org.delafer.xanderView.file.entry.ImageAbstract.ImageType;
import org.delafer.xanderView.file.readers.FileReader;
import org.delafer.xanderView.general.State;
import org.delafer.xanderView.gui.ImageCanvas;
import org.delafer.xanderView.gui.config.ApplConfiguration;
import org.delafer.xanderView.gui.config.ApplInstance;
import org.delafer.xanderView.gui.helpers.MultiShell;
import org.delafer.xanderView.gui.helpers.UIHelpers;
import org.delafer.xanderView.gui.splash.SplashWindow;
import org.delafer.xanderView.interfaces.IAbstractReader.FileEvent;
import org.eclipse.swt.widgets.Display;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyService {

	private static final Logger log = LoggerFactory.getLogger(CopyService.class);

	private static final Map<String, CopyService> multitons = Collections.synchronizedMap(new TreeMap<>());

	public static CopyService getInstance(final String key) {
		return multitons.computeIfAbsent(key, CopyService::new);
	}

	public final static CopyService instance() {
		return getInstance(ApplConfiguration.instance().getCopyDir());
	}

	public static void closeAll() {
		synchronized (multitons) {
			for (CopyService next : multitons.values()) {
				next.close();
			}
			multitons.clear();
		}
	}

	private CopyService(String path) {
		init(path);
	}


    private transient Object lockObj = new Object();

	Set<ImageAbstract<?>> images;
//	ListIterator<ImageFS> iterator;
	File pathFile;
	String pathTxt;
	FileReader reader;
	Boolean initialized = null;
	private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
	private final AtomicBoolean workerStarted = new AtomicBoolean(false);

	public int size() {
		return images.size();
	}


	public static void checkAsync(Observer observer) {
		CopyService cs = CopyService.instance();
		cs.addObserver(observer);
		if (null != cs.initialized && cs.initialized) {
			cs.notifyObservers();
		}
	}

	public static boolean exists(ImageAbstract<?> img) {
		return CopyService.instance().imageExist(img);
	}

	public boolean imageExist(ImageAbstract<?> img) {
		if (img == null) {
			return false;
		}

		final var targetSize = img.size();

		// Traverse the synchronized collection only while holding its monitor.
		// Do not do expensive CRC work here.
		final List<ImageAbstract<?>> candidates;
		synchronized (this.images) {
			candidates = this.images.stream()
					.filter(next -> next.size() == targetSize)
					.toList();
		}
		if (candidates.isEmpty())  return false;
		// Compute searched image CRC once.
		img.CRC();
		var stream = candidates.size() > 8 ? candidates.parallelStream()
				: candidates.stream();
		// Parallelize only the expensive candidate-side CRC/equals work.
		return stream
				.anyMatch(next -> {
					next.CRC();
					return img.equals(next);
				});
	}

	public void init() {
		this.init(ApplConfiguration.instance().getCopyDir());
	}

	private void init(String locationArg) {
		String location = FilePath.as().dir(locationArg).div(true).forceExists().build();
		initializeByPath(location);

	}

	private void initializeByPath(String locationArg) {
		close();
		this.pathTxt = locationArg;
		this.pathFile = new File(locationArg);
		this.reader = new FileReader(pathFile, Recurse.Flat);
		this.images = Collections.synchronizedSet(new HashSet<>());
		initializeInternal();

	}

	private synchronized void close() {
		FileReader old = this.reader;
		this.reader = null;
		if (old != null) {
			old.close();
		}
	}

	private ImageAbstract<?>  findByIdentifier(String ident) {
		synchronized (images) {
			for (ImageAbstract<?> next : images) {
				if (Equals.equal(next.getIdentifier(), ident)) return next;
			}
		}
		return null;

	}


	public void checkInitialized() {
		try {
			synchronized (lockObj) {
				while (null == initialized) lockObj.wait();
			}
		} catch (InterruptedException e) {}
	}

	public void copy(final ImageAbstract<?> entry, final CopyObserver observer) {
			checkInitialized();
			startWorkerIfNeeded();
			ApplInstance.openTasks.incrementAndGet();
			queue.offer(new Runnable() {
				@Override
				public void run() {
					try {
						observer.done(CopyService.this.copySync(entry));
					} finally {
						ApplInstance.openTasks.decrementAndGet();
					}
				}
			});

//			return State.Ignore;
	}

	private void startWorkerIfNeeded() {
		if (workerStarted.compareAndSet(false, true)) {
			Thread worker = new Thread("copyService") {
				@Override
				public void run() {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							Runnable work = queue.take();
							work.run();
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
						}
					}
				}
			};
			worker.setDaemon(true);
			worker.setPriority(Thread.NORM_PRIORITY-2);
			worker.start();
		}
	}


	public State copySync(ImageAbstract<?> entry) {
		try {

//			ImageFS fde = new ImageFS(null,null,entry.size());
//			fde.setCRC(entry.CRC());

			if (exists(entry)) {
//				System.out.println("Already exists!!!");
//				for (ImageDir next : images) {
//					System.out.println(next.identifier+" "+next.name+" "+next.crc);
//				}
				return State.Ignore;
			}

			String fileName = entry.name();
			String baseName = FileUtils.getBaseName(fileName);
			String ext = FileUtils.getExtension(fileName);

			INameIncrementer nameInc = INameIncrementer.instance(baseName);

			File aFile = null;
			while (true) {
				aFile = getFile(ext, nameInc);
				try {
					nioTransferCopy(entry, aFile);
					break;
				} catch (FileAlreadyExistsException conflict) {
					// Another process/thread created the same name between checks; retry next suffix.
					nameInc.increment();
				}
			}

			ImageFS newEntry = new ImageFS(FileUtils.extractFullPathName(aFile), fileName, entry.size());
			newEntry.setCRC(entry.CRC());

			ImageAbstract<?>  removeIt = CopyService.this.findByIdentifier(newEntry.getIdentifier());
			
			removeImage(removeIt);
			addImage(newEntry, false);
			
			return State.Success;

		} catch (Throwable e) {
			log.error("Can't copy image", e);
			return State.Error;
		}


	}

	private void addImage(ImageAbstract<?> add, boolean update) {
		if (add == null) return ;
		if (update && images.size()>0) {
			if (exists(add)) removeImage(add);
		}
		images.add(add);
		
		//debug (traverse the synchronized set only while holding its monitor: watcher and copy worker mutate it)
		boolean ok = false;
		synchronized (images) {
			for (ImageAbstract<?> next : images) {
				if (next == add) {
					ok =true;
					break;
				}
			}
		}
		if (!ok) {
			log.warn("Can't add image!");
		}
	}
	
	private void removeImage(ImageAbstract<?> add) {
		if (add == null) return ;
		if (!images.remove(add)) { // atomic; the former size-before/after check raced with concurrent mutations
			log.warn("Can't remove image: {}", add);
		}
	}


    private static void nioTransferCopy(ImageAbstract<?> entry, File target) throws IOException {
		Buf content = null;
		Path targetPath = target.toPath();
		if (Files.exists(targetPath)) throw new FileAlreadyExistsException(target.getName());
		boolean created = false;
		try {
			content = entry.content();
			if (content == null) {
				throw new EOFException("No content for source entry: " + entry);
			}

			ByteBuffer source = content.get();
			long expected = source.remaining();
			long written = 0;

			Path parent = targetPath.getParent();
			if (parent != null && !Files.isDirectory(parent)) {
				Files.createDirectories(parent);
			}

			try (FileChannel out = FileChannel.open(targetPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)) {
				created = true;
				while (source.hasRemaining()) {
					written += out.write(source);
				}
				if (written != expected) {
					throw new EOFException("Short write: expected " + expected + " bytes but wrote " + written + " bytes");
				}
				// Keep metadata sync disabled for speed; data pages are still forced.
				out.force(false);
			}
		} catch (IOException | RuntimeException e) {
			if (created) {
				try {
					Files.deleteIfExists(targetPath);
				} catch (IOException cleanup) {
					e.addSuppressed(cleanup);
				}
			}
			throw e;
		} finally {
			Streams.closeSilently(content);
		}

		if (ApplConfiguration.instance().isHistory()) {
			try {
				CopyLogger.getInstance().add(entry, target);
			} catch (IOException historyError) {
				log.warn("Can't write copy history entry", historyError);
			}
		}
    }


	private File getFile(String ext, INameIncrementer nameInc) {
		StringBuilder sb = new StringBuilder();
		sb.append(pathTxt);
		sb.append(nameInc.build());
		sb.append('.');
		sb.append(ext);
		File aFile = new File(sb.toString());
		return aFile;
	}

	@SuppressWarnings("unchecked")
	private void initializeInternal() {

		try {
			reader.register(new ContentChangeWatcher() {

				@Override
				public void onEvent(FileEvent type, Object id)throws IOException {
					switch (type) {
					case Create:
						//if (1==1) return ;
						String fileName = id.toString();
						File aFile = new File(fileName);
						if (aFile.isDirectory()) return ;
						ImageType imgType = ImageAbstract.getType(fileName);
						if (ImageType.UNKNOWN.equals(imgType)) return ;
						UIHelpers.sleep(1000);
						ImageAbstract<String> entryNew = (ImageAbstract<String>)reader.getEntryByIdentifier(id);
						addImage(entryNew, true);
						break;
					case Delete:
						ImageAbstract<?>  entryDel = findByIdentifier(""+id);
//						ImageAbstract<?> fde = CopyService.this.findByIdentifier(entryDel.getIdentifier());
						removeImage(entryDel);
						break;
					default:
						log.debug("Other type");
					}

				}

			});


			readStructure();
		} catch (Exception e) {
			log.error("Can't initialize copy service", e);
		}

	}

	protected void readStructure() throws Exception {
		Thread th = new Thread() {

			public void run() {
				synchronized (lockObj) {

				List<ImageAbstract<?>> toAdd = new ArrayList<ImageAbstract<?>>();
				reader.read(toAdd);

				for (ImageAbstract<?> next : toAdd) {
					addImage(((ImageAbstract<?>)next), false);
				}
				reader.initialize();

				initialized = Boolean.TRUE;
				lockObj.notify();
				notifyObservers();
				}
			}

		};
		th.setDaemon(true);
		th.start();

	}

	final Queue<Observer> stack = new LinkedBlockingQueue<>();

	private void notifyObservers() {
		while (!stack.isEmpty()) {
			Observer observer = stack.poll();
			observer.done(this);
		}
	}

	protected void addObserver(Observer observer) {
		stack.add(observer);
	}

//	public void test() {
//		for (ImageDir fileDirEntry : images) {
//			System.out.println(fileDirEntry);
//		}
//	}


//	public void onContainerContentChange() {
//	}

//	public static void main(String[] args) {
//		Queue<String> a = new LinkedList<String>();
//		a.add("b");
//		a.add("x");
//		a.add("a");
//		System.out.println(a.remove());
//		System.out.println(a.remove());
//		System.out.println(a.remove());
//
//	}

	public static class CopyObserver {

		private MultiShell shell;
		private ImageCanvas panel;
		public CopyObserver(MultiShell shell, ImageCanvas canv) {
			this.shell = shell;
			this.panel = canv;
		}

		public void done(final State state) {
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					new SplashWindow(shell.active(), state);

					if (State.Success.equals(state)) {
						panel.setExist(true);
						panel.showImage();
					}

				}

			});
		}

	}

	public interface Observer {
		void done(CopyService instance);
	}

}
