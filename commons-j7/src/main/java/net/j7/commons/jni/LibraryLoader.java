package net.j7.commons.jni;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import net.j7.commons.base.NotNull;
import net.j7.commons.io.*;
import net.j7.commons.io.SysInfo.ArchVM;
import net.j7.commons.io.SysInfo.OS;
import net.j7.commons.io.SysInfo.OSFamily;


public class LibraryLoader {

	/**
	 * Lazy-loaded Singleton, by Bill Pugh *.
	 */
	private static final class Holder {
		/** The Constant INSTANCE. */
		private final static transient LibraryLoader INS = new LibraryLoader();
	}

	/**
	 * Gets the single instance of ResourcesDR.
	 * @return single instance of ResourcesDR
	 */
	public static final LibraryLoader instance() {
		return Holder.INS;
	}

	static Map<String, ArchVM> dmod;
	static Map<String, String> cnv64to32;
	private OS os;
	private String arch;
	private ArchVM model;

	private String basePath;

	static {
		dmod = new HashMap<String, SysInfo.ArchVM>(9);
		dmod.put("ppc", ArchVM.x32);
		dmod.put("i386", ArchVM.x32);
		dmod.put("x86", ArchVM.x32);
		dmod.put("sparc", ArchVM.x32);

		dmod.put("ppc64", ArchVM.x64);
		dmod.put("ia64", ArchVM.x64);
		dmod.put("x86_64", ArchVM.x64);
		dmod.put("amd64", ArchVM.x64);
		dmod.put("sparcv9", ArchVM.x64);

		cnv64to32 = new HashMap<String, String>(4);
		cnv64to32.put("x86_64", "x86");
		cnv64to32.put("ppc64", "ppc");
		cnv64to32.put("amd64", "x86");
		cnv64to32.put("sparcv9", "sparc");
	}


	private LibraryLoader() {
		SysInfo si = SysInfo.instance();
		os = si.getOS();
		arch = getArch();
		model = si.getDataModel();
		basePath = getPath();
	}


	public String getExt() {
		switch (os) {
		case Windows:
		case Windows9xME:
		case WindowsNT5:
			return "dll";
		case MacOS:
			return "jnilib";
		default:
		case Linux:
		case UnixGeneric:
			return "so";
		}
	}

	public String getPath() {
		switch (os) {
		case Windows:
		case Windows9xME:
		case WindowsNT5:
			return "windows";
		case UnixGeneric:
		case Unknown:
			return "linux";
		case MacOS:
		case Linux:
		default:
			return os.name().toLowerCase();
		}
	}


	private String getArch() {

		String val = NotNull.string(SysInfo.instance().archOS()).toLowerCase();

		if (OSFamily.Unix.equals(os.family())) {
			if ("amd64".equals(val)) val = "x86_64";
			else
			if ("i386".equals(val) || "i686".equals(val)) val = "x86";
		}

		if (OSFamily.Windows.equals(os.family())) {
			if ("x86_64".equals(val)) val = "amd64";
			else
			if ("i386".equals(val)) val = "x86";
		}

		if (val.equals ("IA64N")) val = "ia64_32"; //$NON-NLS-1$ $NON-NLS-2$
		if (val.equals ("IA64W")) val = "ia64"; //$NON-NLS-1$ $NON-NLS-2$


		ArchVM vm = dmod.get(val);
		if (ArchVM.x64.equals(vm) && ArchVM.x32.equals(model)) {
			String ret = cnv64to32.get(val);
			if (null != ret) val = ret;
		}


		return val;
	}

	public FilePath getFSPath(String appName, String libraryName) {

		return FilePath.as().dir(SysInfo.instance().getAppDataDir(appName)).file(getJARPath(libraryName, "/"));


	}

	public String getJARPath(String libraryName, String root) {
		StringBuilder sb = new StringBuilder();
		sb.append(root).append("libs");
		sb.append('/').append(basePath);
		sb.append('/').append(arch);
		sb.append('/').append(libraryName);
		sb.append('.').append(getExt());
		return sb.toString();
	}

	/** Per-JVM state of a library name: no source and no cache / available on disk / System.load-ed. */
	private enum State { MISSING, COPIED, LOADED }

	/**
	 * Native libraries that must sit next to the requesting library on Windows, because the native loader
	 * resolves their imports from the requesting DLL's directory (JNA loads with an altered search path).
	 * They are only copied, never force-loaded, so an unneeded entry has no effect.
	 */
	private static final Map<String, String[]> WINDOWS_DEPENDENCIES = Map.of(
			"libavif", new String[] {"libgcc_s_seh-1"});
	private static final String[] NO_DEPENDENCIES = new String[0];

	/** Leftovers of interrupted copies or replaced, still-mapped libraries older than this are swept. */
	private static final long STALE_MILLIS = 10 * 60 * 1000L;

	private final Map<String, State> states = new HashMap<>();
	private final Map<String, Path> paths = new HashMap<>();
	private boolean swept;

	/**
	 * Makes a library available and, for {@code load == true}, loads it via {@link System#load(String)};
	 * for {@code load == false} only points the JNA search path at its directory.
	 * <p>
	 * Synchronized (re-entrant) because {@code System.load} may run JNI_OnLoad code that calls back into the loader.
	 * A missing library is a soft failure: it is reported once on stderr and {@code false} is returned.
	 */
	private synchronized boolean load(String name, boolean load) {
		if (!swept) {
			swept = true;
			sweepStale();
		}
		Path path = resolve(name);
		if (path == null) return false;
		if (load) {
			if (State.LOADED != states.get(name)) {
				try {
					System.load(path.toString());
				} catch (UnsatisfiedLinkError e) {
					// the cache file may be missing for a moment while another instance swaps it: resolve once more
					if (Files.exists(path)) throw e; // state stays COPIED -> retried on a later call
					states.remove(name);
					paths.remove(name);
					path = resolve(name);
					if (path == null) return false;
					System.load(path.toString());
				}
				states.put(name, State.LOADED);
			}
		} else {
			String locDir = path.getParent().toString();
			System.setProperty("jna.platform.library.path", locDir);
			System.setProperty("jna.library.path", locDir);
		}
		return true;
	}

	/**
	 * {@link #prepare(String)} plus the Windows dependencies of {@code name}. A library is kept in the same directory
	 * as its dependencies: if a dependency had to fall back to the source directory, the library is taken from there too.
	 */
	private Path resolve(String name) {
		boolean dependencyOutsideCache = false;
		if (OSFamily.Windows.equals(os.family())) {
			for (String dependency : WINDOWS_DEPENDENCIES.getOrDefault(name, NO_DEPENDENCIES)) {
				Path dependencyPath = prepare(dependency);
				if (null != dependencyPath && !dependencyPath.equals(cachePath(dependency))) dependencyOutsideCache = true;
			}
		}
		Path path = prepare(name);
		if (null != path && dependencyOutsideCache && path.equals(cachePath(name))) {
			Path sourceFile = localFile(getJARPath(name, getLibPath()));
			if (null != sourceFile) {
				path = sourceFile;
				paths.put(name, path);
			} else {
				System.err.println("native library " + name + " is loaded from the cache although a dependency came from the source directory");
			}
		}
		return path;
	}

	/** Returns the file to load for {@code name} (normally the cache file), or null if unavailable; memoized per JVM. */
	private Path prepare(String name) {
		State state = states.get(name);
		if (State.MISSING == state) return null;
		if (null != state) return paths.get(name);
		Path path = ensureCached(name);
		states.put(name, null == path ? State.MISSING : State.COPIED);
		if (null != path) paths.put(name, path);
		return path;
	}

	private static String appName = "xanderView";

	private InputStream fromURL(String url) {
        try {
			URLConnection connection = new URL(url).openConnection();
			connection.setConnectTimeout(10_000);
			connection.setReadTimeout(30_000);
			connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException e) {
			return getClass().getResourceAsStream(url);
        }
    }

	/**
	 * Copies the library into the persistent cache when it is missing there or differs from the source
	 * (exact content comparison, so a rebuilt library of equal size/timestamp is detected too).
	 * The cache file is only ever replaced by a fully written and verified sibling temp file.
	 * @return the file to load: the cache file; the source file itself if a changed library cannot be published
	 * (e.g. the old one is mapped by a running instance); a stale cache file only if there is no other choice;
	 * null if the library is unavailable.
	 */
	private Path ensureCached(String name) {
		Path target = cachePath(name);
		String source = getJARPath(name, getLibPath());
		Path sourceFile = localFile(source);
		boolean cached = Files.isRegularFile(target);
		if (cached && !readable(target)) {
			// an unreadable cache file is treated as stale: replace it, or fall back to the source below
			System.err.println("native library cache file not readable: " + target);
		} else if (cached) {
			Boolean same;
			try {
				same = sameContent(source, sourceFile, target);
			} catch (IOException e) {
				// the cache was just readable, so the source cannot be read (permissions, locks): keep using the cache
				System.err.println("native library source not readable, using cached file " + target + ": " + e);
				return target;
			}
			if (null == same || same) return target; // up to date, or no source to compare with
		}
		try {
			if (publish(source, sourceFile, target)) return target;
		} catch (IOException e) {
			System.err.println("native library cache update failed for " + target + ": " + e);
		}
		if (null != sourceFile) {
			System.err.println("native library cache not updated, loading from source: " + sourceFile);
			return sourceFile;
		}
		if (cached) {
			System.err.println("native library cache not updated, using existing file: " + target);
			return target;
		}
		System.err.println("doesn't exist: " + source);
		return null;
	}

	private Path cachePath(String name) {
		return Paths.get(getFSPath(appName, name).build());
	}

	/** @return true if the file's content can actually be read (detects permission and lock problems). */
	private static boolean readable(Path file) {
		try (InputStream in = Files.newInputStream(file)) {
			in.read();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/** @return the source as a local regular, readable file, or null if it is not such a file: location. */
	private static Path localFile(String source) {
		if (null == source || !source.startsWith("file:")) return null;
		try {
			Path file = Paths.get(new URI(source));
			return Files.isRegularFile(file) && Files.isReadable(file) ? file : null;
		} catch (URISyntaxException | IllegalArgumentException | FileSystemNotFoundException e) {
			return null;
		}
	}

	/** @return TRUE/FALSE when both are readable and (un)equal, null when the source is unavailable. */
	private Boolean sameContent(String source, Path sourceFile, Path target) throws IOException {
		if (null != sourceFile) {
			return Files.size(sourceFile) == Files.size(target) && Files.mismatch(sourceFile, target) == -1L;
		}
		try (InputStream src = fromURL(source)) {
			if (null == src) return null;
			try (InputStream dst = new BufferedInputStream(Files.newInputStream(target))) {
				InputStream in = new BufferedInputStream(src);
				int b;
				do {
					b = in.read();
					if (b != dst.read()) return Boolean.FALSE;
				} while (b != -1);
				return Boolean.TRUE;
			}
		}
	}

	/** Writes the source to a unique sibling temp file, verifies it and moves it over {@code target}. */
	private boolean publish(String source, Path sourceFile, Path target) throws IOException {
		Files.createDirectories(target.getParent());
		Path tmp = target.resolveSibling(target.getFileName() + "." + ProcessHandle.current().pid() + "-" + System.nanoTime() + ".tmp");
		try {
			if (null != sourceFile) {
				System.out.println("copying native library: " + source + " -> " + target);
				Files.copy(sourceFile, tmp);
				if (Files.mismatch(sourceFile, tmp) != -1L) throw new IOException("copy verification failed: " + tmp);
			} else {
				try (InputStream is = fromURL(source)) {
					if (null == is) return false;
					System.out.println("copying native library: " + source + " -> " + target);
					Files.copy(is, tmp); // a failure mid-stream throws, so a partial file is never published
				}
			}
			chmod("755", tmp.toString());
			if (moveIntoPlace(tmp, target)) return true;
			// another instance may have published the same content in the meantime
			return Files.isRegularFile(target) && Files.mismatch(tmp, target) == -1L;
		} finally {
			try { Files.deleteIfExists(tmp); } catch (IOException ignore) {}
		}
	}

	private static boolean moveIntoPlace(Path tmp, Path target) {
		try {
			Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE); // replaces an existing target on Windows (MoveFileEx) and POSIX (rename)
			return true;
		} catch (AtomicMoveNotSupportedException e) {
			try {
				Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
				return true;
			} catch (IOException e2) {
				return false;
			}
		} catch (IOException e) {
			// Windows: a library mapped by a running process cannot be replaced or deleted, but it can be renamed.
			if (!Files.exists(target)) return false;
			Path old = target.resolveSibling(target.getFileName() + ".old-" + System.nanoTime());
			try {
				Files.move(target, old, StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e2) {
				return false;
			}
			try {
				Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException e3) {
				// roll back without replacing: another instance may already have published a winner at target
				try { Files.move(old, target); } catch (IOException ignore) {} // old stays and is swept later
				return false;
			}
			try { Files.deleteIfExists(old); } catch (IOException ignore) {} // fails while still mapped; swept later
			return true;
		}
	}

	/** Deletes leftovers ({@code *.<pid>-<nanos>.tmp}, {@code *.old-<nanos>}) older than {@link #STALE_MILLIS}. */
	private void sweepStale() {
		Path dir = Paths.get(getFSPath(appName, "x").build()).getParent();
		if (null == dir || !Files.isDirectory(dir)) return;
		long limit = System.currentTimeMillis() - STALE_MILLIS;
		try (DirectoryStream<Path> files = Files.newDirectoryStream(dir)) {
			for (Path file : files) {
				String fileName = file.getFileName().toString();
				if (!fileName.matches(".+\\.\\d+-\\d+\\.tmp") && !fileName.matches(".+\\.old-\\d+")) continue;
				try {
					if (Files.getLastModifiedTime(file).toMillis() < limit) Files.deleteIfExists(file);
				} catch (IOException ignore) {} // still mapped by another process: retried on a later start
			}
		} catch (IOException ignore) {}
	}

	void chmod(String permision, String path) {
		if (!OSFamily.Unix.equals(this.os.family())) return; //$NON-NLS-1$
		try {
			Runtime.getRuntime ().exec (new String []{"chmod", permision, path}).waitFor(); //$NON-NLS-1$
		} catch (Throwable e) {}
	}



	public static void loadLibrary(String name) {
		checkLibrary(name, true);
	}

	public static void checkLibrary(String name, boolean load) {
		LibraryLoader.instance().load(name, load);
	}

	private static String fixBs(String str) {
		if (str == null || str.isEmpty()) return str;
		char last = str.charAt(str.length() - 1);
		return (last == '/' || last == '\\') ? str : str + '/';
	}
	private String libPath = null;
	private synchronized String getLibPath() {
		if (libPath == null) {
			libPath = getLocations();
		}
		return libPath;
	}


	private static String getLocations() {
		URL libUrl = LibraryLoader.class.getResource("/libs.properties");
		if (libUrl == null) return "";
        try(InputStream isLibs  = libUrl.openStream()) {
			Properties props = new Properties();
			props.load(isLibs);
			String loc = props.getProperty("location");
			if (loc != null && !loc.isEmpty()) {
				if (loc.startsWith("file:") || loc.startsWith("jar:") || loc.startsWith("http")) {
					return fixBs(loc);
				} else {
					return Paths.get(loc).toUri().toString();
				}
			}

		} catch (IOException ignore) {}
		String loc = libUrl.toExternalForm();
		int idx = loc.lastIndexOf(".jar!");
		return (idx >= 0) ? loc.substring(0, idx+5) + '/' : "";
	}

	public static void main(String[] args) {
		//jar:file:/D:/workspace/j7project/commontools/xanderView/target/xanderview-j7-1.0.0.jar!/dummy.cl
		System.out.println(">>>>>"+LibraryLoader.getLocations());
		File a = new File("W:\\avif\\good\\1.0.4\\libavif.dll");
		System.out.println("1]"+a.getPath());
		System.out.println("2]"+a.getParent());
		System.out.println("3]"+a.getParentFile().toString());

		LibraryLoader.loadLibrary("liblz4-java");
//		LibraryLoader.loadLibrary("lib7-Zip-JBinding");
//		LibraryLoader.loadLibrary("liblz4-java");
	}

}

