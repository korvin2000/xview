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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.j7.commons.base.NotNull;
import net.j7.commons.io.*;
import net.j7.commons.io.SysInfo.ArchVM;
import net.j7.commons.io.SysInfo.OS;
import net.j7.commons.io.SysInfo.OSFamily;


public class LibraryLoader {

	private static final Logger log = LoggerFactory.getLogger(LibraryLoader.class);

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
		model = si.getDataModel(); // before getArch(), which maps 64-bit names for a 32-bit JVM (B6)
		arch = getArch();
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
		if ("arm64".equals(val)) val = "aarch64"; // some JVMs report arm64; the native dir is always aarch64


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
		return libDir(root) + libraryName + '.' + getExt();
	}

	/** {@code <root>libs/<os>/<arch>/} */
	private String libDir(String root) {
		return root + "libs/" + basePath + '/' + arch + '/';
	}

	/** The source location of a library file ({@code <location>/libs/<os>/<arch>/<fileName>}). */
	private String sourceOf(String fileName) {
		return libDir(getLibPath()) + fileName;
	}

	/** The persistent cache file of a library file ({@code <AppDataDir>/libs/<os>/<arch>/<fileName>}). */
	private Path cacheOf(String fileName) {
		return Paths.get(FilePath.as().dir(SysInfo.instance().getAppDataDir(appName)).file(libDir("/") + fileName).build());
	}

	/** Load name → chosen file name ({@code stem.ext}), memoized per JVM. */
	private final Map<String, String> fileNames = new HashMap<>();

	/**
	 * The file ({@code stem.ext}) behind a load name (PLAN P5.2). Windows: always {@code <name>.dll}, as before.
	 * Unix also tries {@code lib<name>} when the name lacks the prefix (e.g. {@code turbojpeg} → {@code libturbojpeg.so});
	 * macOS tries {@code .dylib} before {@code .jnilib}, and only {@code .dylib} for JNA libraries.
	 * A candidate present in the source wins over one present only in the cache, so a stale cache file never shadows a
	 * new source file; with neither, the first candidate is the one reported as missing.
	 */
	private String fileName(String name, boolean jna) {
		String known = fileNames.get(name);
		if (null != known) return known;
		List<String> candidates = fileCandidates(name, jna);
		String chosen = null;
		if (candidates.size() > 1) {
			for (String candidate : candidates) {
				if (sourceExists(candidate)) { chosen = candidate; break; }
			}
			if (null == chosen) {
				for (String candidate : candidates) {
					if (Files.isRegularFile(cacheOf(candidate))) { chosen = candidate; break; }
				}
			}
		}
		if (null == chosen) chosen = candidates.get(0);
		fileNames.put(name, chosen);
		return chosen;
	}

	/** Candidate file names in preference order: name-major ({@code name.ext1, name.ext2, libname.ext1, …}). */
	List<String> fileCandidates(String name, boolean jna) {
		List<String> exts = OS.MacOS == os ? (jna ? List.of("dylib") : List.of("dylib", "jnilib")) : List.of(getExt());
		List<String> stems = new ArrayList<>(2);
		stems.add(name);
		if (OSFamily.Unix == os.family() && !name.startsWith("lib")) stems.add("lib" + name);
		List<String> candidates = new ArrayList<>(stems.size() * exts.size());
		for (String stem : stems) {
			for (String ext : exts) candidates.add(stem + '.' + ext);
		}
		return candidates;
	}

	/** @return true if the source has this file (a readable local file, or an openable URL/classpath resource). */
	private boolean sourceExists(String fileName) {
		String source = sourceOf(fileName);
		if (null != localFile(source)) return true;
		if (source.startsWith("file:")) return false;
		try (InputStream in = fromURL(source)) {
			return null != in;
		} catch (IOException e) {
			return false;
		}
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
	 * A missing library is a soft failure: it is reported once as a warning and {@code false} is returned.
	 */
	private synchronized boolean load(String name, boolean load) {
		if (!swept) {
			swept = true;
			sweepStale();
		}
		Path path = resolve(name, !load);
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
					fileNames.remove(name);
					path = resolve(name, false);
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
	 * {@link #prepare(String, boolean)} plus the Windows dependencies of {@code name}. A library is kept in the same directory
	 * as its dependencies: if a dependency had to fall back to the source directory, the library is taken from there too.
	 */
	private Path resolve(String name, boolean jna) {
		boolean dependencyOutsideCache = false;
		if (OSFamily.Windows.equals(os.family())) {
			for (String dependency : WINDOWS_DEPENDENCIES.getOrDefault(name, NO_DEPENDENCIES)) {
				Path dependencyPath = prepare(dependency, false);
				if (null != dependencyPath && !dependencyPath.equals(cachePath(dependency))) dependencyOutsideCache = true;
			}
		}
		Path path = prepare(name, jna);
		if (null != path && dependencyOutsideCache && path.equals(cachePath(name))) {
			Path sourceFile = localFile(sourceOf(fileName(name, jna)));
			if (null != sourceFile) {
				path = sourceFile;
				paths.put(name, path);
			} else {
				log.warn("native library {} is loaded from the cache although a dependency came from the source directory", name);
			}
		}
		return path;
	}

	/** Returns the file to load for {@code name} (normally the cache file), or null if unavailable; memoized per JVM. */
	private Path prepare(String name, boolean jna) {
		State state = states.get(name);
		if (State.MISSING == state) return null;
		if (null != state) return paths.get(name);
		fileName(name, jna); // pick the file once; cachePath/ensureCached use the memoized choice
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
		String source = sourceOf(fileName(name, false));
		Path sourceFile = localFile(source);
		boolean cached = Files.isRegularFile(target);
		if (cached && !readable(target)) {
			// an unreadable cache file is treated as stale: replace it, or fall back to the source below
			log.warn("native library cache file not readable: {}", target);
		} else if (cached) {
			Boolean same;
			try {
				same = sameContent(source, sourceFile, target);
			} catch (IOException e) {
				// the cache was just readable, so the source cannot be read (permissions, locks): keep using the cache
				log.warn("native library source not readable, using cached file {}: {}", target, e.toString());
				return target;
			}
			if (null == same || same) return target; // up to date, or no source to compare with
		}
		try {
			if (publish(source, sourceFile, target)) return target;
		} catch (IOException e) {
			log.warn("native library cache update failed for {}: {}", target, e.toString());
		}
		if (null != sourceFile) {
			log.warn("native library cache not updated, loading from source: {}", sourceFile);
			return sourceFile;
		}
		if (cached) {
			log.warn("native library cache not updated, using existing file: {}", target);
			return target;
		}
		log.warn("doesn't exist: {}", source);
		return null;
	}

	/** The cache file of a load name; {@link #fileName(String, boolean)} has already been chosen by {@link #prepare}. */
	private Path cachePath(String name) {
		return cacheOf(fileName(name, false));
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
				log.info("copying native library: {} -> {}", source, target);
				Files.copy(sourceFile, tmp);
				if (Files.mismatch(sourceFile, tmp) != -1L) throw new IOException("copy verification failed: " + tmp);
			} else {
				try (InputStream is = fromURL(source)) {
					if (null == is) return false;
					log.info("copying native library: {} -> {}", source, target);
					Files.copy(is, tmp); // a failure mid-stream throws, so a partial file is never published
				}
			}
			makeExecutable(tmp);
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

	/** rwxr-xr-x on Unix, like the former {@code chmod 755}, without spawning a process (dlopen itself only needs read). */
	void makeExecutable(Path file) {
		if (!OSFamily.Unix.equals(this.os.family())) return;
		File f = file.toFile();
		f.setReadable(true, false);
		f.setExecutable(true, false);
	}

	public static void loadLibrary(String name) {
		checkLibrary(name, true);
	}

	public static void checkLibrary(String name, boolean load) {
		LibraryLoader.instance().load(name, load);
	}

	/**
	 * Like {@link #loadLibrary(String)}, but tells the caller whether the library is now loaded, so it can fail fast
	 * (used by the patched stock-SWT {@code Library}, whose callers rely on an {@link UnsatisfiedLinkError}).
	 * @return false if the library is unavailable (missing in the source and in the cache)
	 */
	public static boolean tryLoadLibrary(String name) {
		return LibraryLoader.instance().load(name, true);
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


	/** System property that overrides the natives source {@code location} (PLAN P1.9). */
	public static final String LOCATION_PROPERTY = "xview.libs.location";
	/** Environment variable that overrides the natives source {@code location}, below the system property. */
	public static final String LOCATION_ENV = "XVIEW_LIBS_LOCATION";

	/**
	 * The natives source root, ending with '/'. Precedence: {@value #LOCATION_PROPERTY} system property,
	 * {@value #LOCATION_ENV} environment variable, {@code location} in {@code /libs.properties}, then the jar that
	 * contains {@code libs.properties}; "" if none (cache only). Values are a directory path or a
	 * {@code file:}/{@code jar:}/{@code http(s):} URL.
	 */
	private static String getLocations() {
		String override = System.getProperty(LOCATION_PROPERTY);
		if (override == null || override.isBlank()) override = System.getenv(LOCATION_ENV);
		if (override != null && !override.isBlank()) return toLocation(override.trim());

		URL libUrl = LibraryLoader.class.getResource("/libs.properties");
		if (libUrl == null) return "";
        try(InputStream isLibs  = libUrl.openStream()) {
			Properties props = new Properties();
			props.load(isLibs);
			String loc = props.getProperty("location");
			if (loc != null && !loc.isEmpty()) return toLocation(loc);

		} catch (IOException ignore) {}
		String loc = libUrl.toExternalForm();
		int idx = loc.lastIndexOf(".jar!");
		return (idx >= 0) ? loc.substring(0, idx+5) + '/' : "";
	}

	private static String toLocation(String loc) {
		if (loc.startsWith("file:") || loc.startsWith("jar:") || loc.startsWith("http")) return fixBs(loc);
		return fixBs(Paths.get(loc).toUri().toString()); // toUri() omits the trailing '/' for a missing directory
	}

}

