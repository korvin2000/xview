package org.delafer.xanderView.gui.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.j7.commons.io.FilePath;
import net.j7.commons.io.FilePath.PathType;
import net.j7.commons.io.TextFileUtils;
import net.j7.commons.io.TextFileUtils.TextWriter;
import net.j7.commons.utils.BooleanUtils;
import net.sourceforge.jiu.util.Sort;
import org.delafer.xanderView.common.CopyLogger;
import org.eclipse.swt.internal.win32.SIZE;

public class ApplConfiguration {


//	Map<Long,Long>
//	5 mln. entries
//	2 x 5 000 000 x 8 (64bit)
//
//	Ideal(raw)    80 000 000
//	Hppc:	     134 210 568
//	Kolobok:     134 232 880
//	Trove Mem:   223 680 080
//	GS: Mem:     268 437 960
//	Colt:        269 138 448
//	Java HashMap 432 998 640

	public static final String XANDER_VIEW = "xanderView";
	public static final String CFG_POS_X = "PosX";
	public static final String CFG_POS_Y = "PosY";
	public static final String CFG_WIDTH = "Width";
	public static final String CFG_HEIGHT = "Height";
	public static final String CFG_FULLSCREEN = "fullscreen";
	public static final String SCALER = "Scaler";
	public static final String LOOP_CURRENT_SOURCE = "loop.source";
	private static final String CFG_COPY_DIR = "target.folder";
	public static final String CFG_CRY_DIR = "cry.keyfile";
	public static final String CFG_SORT = "sort";
	public static final String CFG_USE_CSV = "csv";
	public static final String CFG_RECURSIVE = "recursive";
	public static final String CFG_FILTER_COUNT = "filterCount";
	public static final String CFG_HISTORY = "history";

	public enum SortType {
		UNSORTED,
		NO,
		NAME,
		SIZE,
		RANDOM,
		NATURAL,
		DATE,
		DATETIME,
		TIME
	}

	public enum SortDir {
		ASC,
		DESC
	}
	/**
	 * Lazy-loaded Singleton, by Bill Pugh *.
	 */
	private static final class Holder {
		/** The Constant INSTANCE. */
		private final static transient ApplConfiguration INSTANCE = new ApplConfiguration();
	}

	/**
	 * Gets the single instance of ResourcesDR.
	 *
	 * @return single instance of ResourcesDR
	 */
	public static ApplConfiguration instance() {
		return Holder.INSTANCE;
	}


	private Properties properties;
	private boolean unsaved;
	private String config;

	private ApplConfiguration() {
		unsaved = false;
		initialize();
	}

	private Properties defaults() {
		Properties pro = new Properties();
		return defaults(pro);
	}

	private Properties defaults(Properties pro) {
		pro.setProperty(CFG_HEIGHT, String.valueOf(600));
		pro.setProperty(CFG_WIDTH, String.valueOf(800));
		pro.setProperty(SCALER, "1;11,500000;10,665000;6,1850000;5,2500000;6;6000000;4,8000000;2,13000000");
		pro.setProperty(LOOP_CURRENT_SOURCE, String.valueOf(true));
		pro.setProperty(CFG_COPY_DIR, "A:\\target\\src");
		pro.setProperty(CFG_FULLSCREEN, "1");
		pro.setProperty(CFG_CRY_DIR, "A:\\key");
		pro.setProperty(CFG_SORT, "size,desc");
		pro.setProperty(CFG_USE_CSV, "1");
		return pro;
	}

	private void initialize() {
		config = FilePath.as().dir(PathType.ApplicationData).dir(XANDER_VIEW).file("xander.properties").forceExists().build();
		properties = defaults();
		try {
		  properties.load(new FileInputStream(config));
		} catch (IOException e) {
		  e.printStackTrace();
		}

		final AtomicInteger started = new AtomicInteger(0);
	    TimerTask task = new TimerTask() {
			public void run() {
				if (started.incrementAndGet() % 8 == 0) {
					CopyLogger.flushIfNeeded();
				}
				if (ApplConfiguration.this.isDirty()) {
					ApplConfiguration.this.save();
				}
			}
	    };

	    ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(threadFactory);
	    timer.scheduleWithFixedDelay(task, 5, 8, TimeUnit.SECONDS);
	}


	public String get(String key) {
		return properties.getProperty(key);
	}

	public long getLong(String key) {
		try {
			return Long.parseLong(get(key));
		} catch (Exception i) { return 0L; }
	}

	public int getInt(String key) {
		try {
			return Integer.parseInt(get(key));
		} catch (Exception i) { return 0; }
	}

	public String[] getValues(String key) {
		String str = get(key);
		return str.split(";");
	}

	public double getDouble(String key) {
		try {
			return Double.parseDouble(get(key));
		} catch (Exception i) { return 0d; }
	}

	public boolean getBoolean(String key) {
		try {
			return BooleanUtils.toBoolean(get(key));
		} catch (Exception i) {
			return false;
		}
	}

	private Boolean history;
	public boolean isHistory() {
		if (history == null) {
			history = getBoolean(CFG_HISTORY);
		}
		return history;
	}

	public SortBy getSort() {
		return new SortBy(this.get(CFG_SORT));
	}
	static int filterCount = -1;
	public static int getFilterCount() {
		if (filterCount == -1) {
			filterCount = ApplConfiguration.instance().getInt(CFG_FILTER_COUNT);
		}
		return filterCount;
	}

	public boolean isDirty() {
		return unsaved;
	}
	private String pwd;
	
	public synchronized String pwd() {
		if (null == pwd)
		try {
			Path pth = Paths.get(get(CFG_CRY_DIR));
			pwd = Files.exists(pth) ? new String(Files.readAllBytes(pth), "utf-8").trim() : "";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pwd;
	}
	
	public boolean hasPwd() {
		String pwd = pwd();
		return pwd != null && !pwd.isEmpty();
	}

	public void set(String key, Object value) {
		String toSet = value != null ? value.toString() : "null" ;
		if (toSet.equals(get(key))) return ;
		unsaved = true;
		properties.setProperty(key, toSet);
	}

	public void set(String key, String[] values) {
		StringBuilder sb = new StringBuilder();
		int j = 0;
		for (String next : values) {
			if (j++>0) sb.append(';');
			sb.append(next);
		}
		set(key, sb);
	}

	public void save() {
		try {
			FileOutputStream fos = new FileOutputStream(config);
			properties.store(fos, null);
			fos.close();


			if (ApplInstance.lastEntry != null && this.getBoolean(CFG_USE_CSV)) {
				try {
					TextWriter tw = TextFileUtils.createTextWriter(ApplInstance.LAST_ENTRY, false);
					tw.write(ApplInstance.lastEntry);
					tw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}



		} catch (Exception e) {
			e.printStackTrace();
		}
		unsaved = false;

	}

	private final ThreadFactory threadFactory = new ThreadFactory() {

		final static int prio = (int)((Thread.MIN_PRIORITY  + Thread.NORM_PRIORITY ) / 2);

	    public Thread newThread(Runnable r) {
	        Thread t = new Thread(r, "Cfg-"+System.identityHashCode(ApplConfiguration.this));
	        t.setDaemon(true);
	        t.setPriority(prio);
	        return t;
	    }
	};

	public static class SortBy {
		public SortType sort = SortType.UNSORTED;
		public boolean asc = true;

		public SortBy(String sort) {
			if (null != sort) {
				String[] sortArr = sort.split(",");
				try {
					this.sort = SortType.valueOf(sortArr[0].trim().toUpperCase());
					this.asc =  sortArr.length > 1 ? SortDir.valueOf(sortArr[1].trim().toUpperCase()).equals(SortDir.ASC) : true;
				} catch (IllegalArgumentException ignore) {}
			}
		}

		public SortType getSort() {
			return sort;
		}

		public boolean isAsc() {
			return asc;
		}

//		@Override
//		public String toString() {
//			return new StringJoiner(", ", SortBy.class.getSimpleName() + "[", "]")
//					.add("sort=" + sort)
//					.add("asc=" + asc)
//					.toString();
//		}
	}

//	public static void main(String[] args) {
//		System.out.println(new ApplConfiguration.SortBy(null));
//		System.out.println(new ApplConfiguration.SortBy(""));
//		System.out.println(new ApplConfiguration.SortBy("Name"));
//		System.out.println(new ApplConfiguration.SortBy("name,asc"));
//		System.out.println(new ApplConfiguration.SortBy("NAME,desc"));
//		System.out.println(new ApplConfiguration.SortBy("date, desc"));
//		System.out.println(new ApplConfiguration.SortBy("date, asc"));
//		System.out.println(new ApplConfiguration.SortBy("SIZE,ASC"));
//	}

	private static String[] dirs;
	private String[] getCopyDirs() {
		if (null == dirs) {
			String dest = this.get(CFG_COPY_DIR);
			dirs = dest.split(";");
		}
		return dirs;
	}
	private static int currentDir = 0;
	public String nextCopyDir() {
		if (++currentDir == getCopyDirs().length) {
			currentDir = 0;
		}
		return getCopyDir();
	}
	public String prevCopyDir() {
		if (--currentDir < 0) {
			currentDir = getCopyDirs().length - 1;
		}
		return getCopyDir();
	}
	public int currentDir() {
		return this.currentDir;
	}
	public String getCopyDir() {
		return getCopyDirs()[currentDir];
	}

	public void setCopyDir(String dir) {
		getCopyDirs()[currentDir] = dir;
	}

	public boolean isRecursive() {
		return this.getBoolean(CFG_RECURSIVE);
	}

}
