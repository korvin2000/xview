package net.j7.commons.jni;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.j7.commons.base.NotNull;
import net.j7.commons.io.*;
import net.j7.commons.io.SysInfo.ArchVM;
import net.j7.commons.io.SysInfo.OS;
import net.j7.commons.io.SysInfo.OSFamily;


/**
 * Makes native libraries available from {@code <location>/libs/<os>/<arch>/} through a persistent per-user cache
 * ({@code <AppData>/libs/<os>/<arch>/}) and loads them (JNI: {@code System.load}; JNA: {@code jna.library.path}).
 * <p>
 * Two modes (docs/NATIVES.md §4, PLAN P7.2):
 * <ul>
 * <li><b>Manifest mode</b>: the source directory carries {@code natives.sha256} ({@code sha256sum} format) and
 * optionally {@code natives.deps}. The cache is content-addressed: {@code <AppData>/libs/<os>/<arch>/<bundle12>/},
 * where {@code bundle12} is derived from the manifest. A library is copied on its first use, verified against the
 * manifest and then trusted (one {@code stat} per start). Bundle directories are immutable, so several installed
 * versions share one cache root without interference.</li>
 * <li><b>Legacy mode</b> (no manifest): the flat cache directory with an exact content comparison against the source
 * on first use per JVM, as before P7.</li>
 * </ul>
 */
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

	/** The flat (legacy) cache directory {@code <AppDataDir>/libs/<os>/<arch>/}. */
	private Path legacyCacheDir() {
		return Paths.get(FilePath.as().dir(SysInfo.instance().getAppDataDir(appName)).file(libDir("/") + "x").build()).getParent();
	}

	/** The cache file of a file name: the bundle directory in manifest mode, the flat directory otherwise. */
	private Path cacheOf(String fileName) {
		Path bundle = bundleDir();
		return (null != bundle ? bundle : legacyCacheDir()).resolve(fileName);
	}

	/** Load name → chosen file name ({@code stem.ext}), memoized per JVM. */
	private final Map<String, String> fileNames = new HashMap<>();

	/**
	 * The file ({@code stem.ext}) behind a load name (PLAN P5.2). Windows: always {@code <name>.dll}, as before.
	 * Unix also tries {@code lib<name>} when the name lacks the prefix (e.g. {@code turbojpeg} → {@code libturbojpeg.so});
	 * macOS tries {@code .dylib} before {@code .jnilib}, and only {@code .dylib} for JNA libraries.
	 * In manifest mode the first candidate listed in the manifest wins. Otherwise a candidate present in the source wins
	 * over one present only in the cache, so a stale cache file never shadows a new source file; with neither, the first
	 * candidate is the one reported as missing.
	 */
	private String fileName(String name, boolean jna) {
		String known = fileNames.get(name);
		if (null != known) return known;
		List<String> candidates = fileCandidates(name, jna);
		String chosen = null;
		Manifest manifest = authority();
		if (null != manifest) {
			for (String candidate : candidates) {
				if (manifest.sha256.containsKey(candidate)) { chosen = candidate; break; }
			}
		}
		if (null == chosen && candidates.size() > 1) {
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

	/** Per-JVM state of a library name: no source and no cache / available on disk / System.load-ed / System.load failed. */
	public enum State { MISSING, COPIED, LOADED, FAILED }

	// ---------------------------------------------------------------------------------------------------------------
	// Manifests (docs/NATIVES.md §3-4)

	/** Manifest file name in {@code libs/<os>/<arch>/}: {@code sha256sum} format, one line per native. */
	public static final String MANIFEST = "natives.sha256";
	/** Dependency list in {@code libs/<os>/<arch>/}: {@code <file>: <dep> <dep>…}; a {@code ?} suffix marks an optional dependency. */
	public static final String DEPS = "natives.deps";

	/** The parsed {@link #MANIFEST} (+ {@link #DEPS}) of one {@code libs/<os>/<arch>/} directory. */
	static final class Manifest {
		/** First 12 hex digits of SHA-256 over the manifest text: the bundle id and cache sub-directory. */
		final String id;
		/** file name → SHA-256 hex, in manifest order. */
		final Map<String, String> sha256 = new LinkedHashMap<>();
		/** file name → dependency file names in load order (optional ones keep their {@code ?} suffix). */
		final Map<String, List<String>> deps = new LinkedHashMap<>();

		private Manifest(String id) {
			this.id = id;
		}

		static Manifest parse(String shaText, String depsText) throws IOException {
			Manifest m = new Manifest(HexFormat.of().formatHex(sha256(shaText.getBytes(StandardCharsets.UTF_8))).substring(0, 12));
			for (String raw : shaText.split("\n")) {
				String line = raw.trim();
				if (line.isEmpty() || line.startsWith("#")) continue;
				int sp = line.indexOf(' ');
				if (sp != 64) throw new IOException("bad manifest line: " + line);
				String hex = line.substring(0, 64).toLowerCase();
				String file = line.substring(sp).trim();
				if (file.startsWith("*")) file = file.substring(1);
				if (!hex.matches("[0-9a-f]{64}") || !plainFileName(file)) throw new IOException("bad manifest line: " + line);
				m.sha256.put(file, hex);
			}
			if (null != depsText) {
				for (String raw : depsText.split("\n")) {
					String line = raw.trim();
					if (line.isEmpty() || line.startsWith("#")) continue;
					int colon = line.indexOf(':');
					if (colon <= 0) throw new IOException("bad dependency line: " + line);
					String file = line.substring(0, colon).trim();
					List<String> deps = new ArrayList<>();
					for (String dep : line.substring(colon + 1).trim().split("\\s+")) {
						if (dep.isEmpty()) continue;
						if (!plainFileName(dep.endsWith("?") ? dep.substring(0, dep.length() - 1) : dep)) throw new IOException("bad dependency line: " + line);
						deps.add(dep);
					}
					m.deps.put(file, deps);
				}
			}
			return m;
		}

		static boolean plainFileName(String file) {
			return !file.isEmpty() && !file.equals(".") && !file.equals("..") && file.indexOf('/') < 0 && file.indexOf('\\') < 0 && file.indexOf(':') < 0;
		}
	}

	private boolean manifestsRead;
	/** The manifest packed into the application jar (classpath {@code /libs/<os>/<arch>/natives.sha256}), or null. */
	private Manifest expected;
	/** The manifest found in the source directory, or null (legacy mode). */
	private Manifest source;
	/** The manifest files are verified against: the source's own in override mode, else the jar's. Null in legacy mode. */
	private Manifest authority;
	private Path bundleDir;

	private synchronized void readManifests() {
		if (manifestsRead) return;
		manifestsRead = true;
		String libs = libDir("/");
		expected = readManifest(() -> LibraryLoader.class.getResourceAsStream(libs + MANIFEST), () -> LibraryLoader.class.getResourceAsStream(libs + DEPS), "classpath:" + libs);
		source = readManifest(() -> openSource(MANIFEST), () -> openSource(DEPS), sourceOf(""));
		if (null == source) {
			if (null != expected) log.info("natives source {} has no {}: legacy mode (content comparison)", sourceOf(""), MANIFEST);
			return;
		}
		boolean override = null != overrideLocation();
		if (null != expected && !expected.id.equals(source.id)) {
			if (override) {
				log.warn("natives override in effect: {} (bundle {}) differs from the manifest packed in the jar (bundle {})", sourceOf(""), source.id, expected.id);
			} else {
				log.error("natives at {} (bundle {}) do not match the manifest packed in the jar (bundle {}): only files matching the jar's manifest are trusted",
						sourceOf(""), source.id, expected.id);
			}
		}
		authority = (override || null == expected) ? source : expected;
		bundleDir = legacyCacheDir().resolve(authority.id);
		log.debug("natives bundle {} from {}", authority.id, sourceOf(""));
	}

	private interface Opener { InputStream open() throws IOException; }

	private static Manifest readManifest(Opener sha, Opener deps, String where) {
		String shaText = readText(sha);
		if (null == shaText) return null;
		try {
			return Manifest.parse(shaText, readText(deps));
		} catch (IOException e) {
			log.error("ignoring unreadable natives manifest at {}: {}", where, e.getMessage());
			return null;
		}
	}

	private static String readText(Opener opener) {
		try (InputStream in = opener.open()) {
			return null == in ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	/** Opens {@code <location>/libs/<os>/<arch>/<fileName>}: a local file, or a URL/classpath resource; null if absent. */
	private InputStream openSource(String fileName) throws IOException {
		String source = sourceOf(fileName);
		Path local = localFile(source);
		if (null != local) return Files.newInputStream(local);
		if (source.startsWith("file:") || getLibPath().isEmpty()) return null;
		return fromURL(source);
	}

	private Manifest authority() {
		readManifests();
		return authority;
	}

	private Path bundleDir() {
		readManifests();
		return bundleDir;
	}

	/**
	 * Dependencies that must be loaded before {@code fileName}, from {@link #DEPS} and the built-in map. They are
	 * {@code System.load}-ed by absolute path first; the dynamic linker then resolves the dependent library's import
	 * by module name / soname / install name against the already loaded image. Windows: {@code opencv_java460}
	 * needs {@code concrt140.dll}, which the JDK does not ship (optional: loaded only when the bundle has it).
	 */
	private List<String> dependenciesOf(String fileName) {
		Set<String> deps = new LinkedHashSet<>();
		Manifest manifest = authority();
		if (null != manifest) {
			List<String> listed = manifest.deps.get(fileName);
			if (null != listed) deps.addAll(listed);
		}
		if (OSFamily.Windows.equals(os.family()) && "opencv_java460.dll".equals(fileName)) deps.add("concrt140.dll?");
		return new ArrayList<>(deps);
	}

	/** Leftovers of interrupted copies or replaced, still-mapped libraries older than this are swept (legacy directory). */
	private static final long STALE_MILLIS = 10 * 60 * 1000L;

	private final Map<String, State> states = new HashMap<>();
	private final Map<String, Path> paths = new HashMap<>();
	/** File names (dependencies and {@link #checkAll()} subjects) already passed to {@code System.load}. */
	private final Set<String> loadedFiles = new LinkedHashSet<>();
	/** File-level outcomes of dependencies and {@link #checkAll()} that have no load name of their own. */
	private final Map<String, State> fileStates = new HashMap<>();
	private boolean swept;

	/**
	 * Makes a library available and, for {@code load == true}, loads it via {@link System#load(String)};
	 * for {@code load == false} only points the JNA search path at its directory.
	 * <p>
	 * Synchronized (re-entrant) because {@code System.load} may run JNI_OnLoad code that calls back into the loader.
	 * A missing library is a soft failure: it is reported once as a warning and {@code false} is returned.
	 * A present but unloadable library throws the {@link UnsatisfiedLinkError} (state {@link State#FAILED}).
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
					Path retry = recover(name, path, e);
					if (null == retry) return false;
					try {
						System.load(retry.toString());
					} catch (UnsatisfiedLinkError e2) {
						states.put(name, State.FAILED);
						throw e2;
					}
				}
				states.put(name, State.LOADED);
				loadedFiles.add(fileNames.get(name));
			}
		} else {
			String locDir = path.getParent().toString();
			System.setProperty("jna.platform.library.path", locDir);
			System.setProperty("jna.library.path", locDir);
		}
		return true;
	}

	/**
	 * After a failed {@code System.load}: the cache file may be missing for a moment while another instance swaps it
	 * (legacy mode), or a trusted bundle file may be corrupt (manifest mode: re-verified, replaced once).
	 * @return the path to retry with, or null (unavailable, already warned); rethrows {@code e} for an intact file
	 */
	private Path recover(String name, Path path, UnsatisfiedLinkError e) {
		String fileName = fileNames.get(name);
		Manifest manifest = authority();
		if (null != manifest && null != bundleDir && path.startsWith(bundleDir)) {
			String hash = manifest.sha256.get(fileName);
			if (null != hash && Files.isRegularFile(path) && hash.equals(sha256Of(path))) {
				states.put(name, State.FAILED);
				throw e; // intact but not loadable here: wrong architecture, missing system library, ...
			}
			log.warn("native library cache file {} is corrupt or vanished, copying it again", path);
			try { Files.deleteIfExists(path); } catch (IOException ignore) {}
		} else if (Files.exists(path)) {
			states.put(name, State.FAILED);
			throw e; // state stays FAILED; a later call retries System.load
		}
		states.remove(name);
		paths.remove(name);
		fileNames.remove(name);
		return resolve(name, false);
	}

	/**
	 * {@link #prepare(String, boolean)} plus the dependencies of the chosen file, which are made available and
	 * {@code System.load}-ed first (see {@link #dependenciesOf(String)}).
	 */
	private Path resolve(String name, boolean jna) {
		Path path = prepare(name, jna);
		if (null == path) return null;
		loadDependencies(fileNames.get(name));
		return path;
	}

	private void loadDependencies(String fileName) {
		for (String dep : dependenciesOf(fileName)) {
			boolean optional = dep.endsWith("?");
			String depFile = optional ? dep.substring(0, dep.length() - 1) : dep;
			if (loadedFiles.contains(depFile)) continue;
			Path depPath = prepareFile(depFile, optional);
			if (null == depPath) {
				if (!optional) {
					fileStates.put(depFile, State.MISSING);
					log.warn("dependency {} of native library {} is unavailable", depFile, fileName);
				}
				continue;
			}
			loadDependencies(depFile);
			try {
				System.load(depPath.toString());
				loadedFiles.add(depFile);
			} catch (UnsatisfiedLinkError e) {
				fileStates.put(depFile, State.FAILED);
				log.warn("dependency {} of native library {} failed to load: {}", depFile, fileName, e.getMessage());
			}
		}
	}

	/** Returns the file to load for {@code name} (normally the cache file), or null if unavailable; memoized per JVM. */
	private Path prepare(String name, boolean jna) {
		State state = states.get(name);
		if (State.MISSING == state) return null;
		if (null != state) return paths.get(name);
		String fileName = fileName(name, jna); // pick the file once; cachePath/ensureCached use the memoized choice
		Path path = prepareFile(fileName, false);
		states.put(name, null == path ? State.MISSING : State.COPIED);
		if (null != path) paths.put(name, path);
		return path;
	}

	/** The file to load for an exact file name: manifest mode → bundle file, legacy mode → flat cache file; null if unavailable. */
	private Path prepareFile(String fileName, boolean quiet) {
		return null != authority() ? ensureBundled(fileName, quiet) : ensureCachedFile(fileName, quiet);
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

	// ---------------------------------------------------------------------------------------------------------------
	// Manifest mode: content-addressed bundle directory

	/**
	 * Manifest mode: returns {@code <bundleDir>/<fileName>}, copying it from the source on first use. The copy is
	 * written to a sibling temp file, verified against the manifest's SHA-256 and moved into place atomically; a file
	 * that is already there was verified by whoever published it and is trusted. Null if the file is not listed in the
	 * manifest, missing in the source, or fails verification (logged unless {@code quiet}).
	 */
	private Path ensureBundled(String fileName, boolean quiet) {
		Manifest manifest = authority();
		Path target = bundleDir.resolve(fileName);
		if (Files.isRegularFile(target)) return target;
		String hash = manifest.sha256.get(fileName);
		if (null == hash) {
			if (!quiet) log.warn("native library {} is not listed in the natives manifest (bundle {})", fileName, manifest.id);
			return null;
		}
		String source = sourceOf(fileName);
		Path sourceFile = localFile(source);
		Path tmp = null;
		try {
			Files.createDirectories(bundleDir);
			tmp = target.resolveSibling(target.getFileName() + "." + ProcessHandle.current().pid() + "-" + System.nanoTime() + ".tmp");
			if (null != sourceFile) {
				Files.copy(sourceFile, tmp);
			} else {
				try (InputStream is = openSource(fileName)) {
					if (null == is) {
						if (!quiet) log.warn("doesn't exist: {}", source);
						return null;
					}
					Files.copy(is, tmp);
				}
			}
			String actual = sha256Of(tmp);
			if (!hash.equals(actual)) throw new IOException("checksum mismatch for " + source + ": expected " + hash + ", got " + actual);
			makeExecutable(tmp);
			removeQuarantine(tmp);
			log.info("native library verified and cached: {} -> {}", source, target);
			try {
				Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
			} catch (FileAlreadyExistsException | AccessDeniedException e) {
				if (!Files.isRegularFile(target)) throw e; // another instance published the same verified content meanwhile
			}
			return target;
		} catch (IOException e) {
			if (!quiet) log.warn("native library {} unavailable: {}", fileName, e.toString());
			return null;
		} finally {
			if (null != tmp) try { Files.deleteIfExists(tmp); } catch (IOException ignore) {}
		}
	}

	private static byte[] sha256(byte[] data) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/** Lower-case SHA-256 hex of a file, or null if unreadable. */
	static String sha256Of(Path file) {
		try (InputStream in = Files.newInputStream(file)) {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] buf = new byte[1 << 16];
			int n;
			while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
			return HexFormat.of().formatHex(md.digest());
		} catch (IOException | NoSuchAlgorithmException e) {
			return null;
		}
	}

	/** macOS: a copy of a downloaded file may keep the quarantine attribute (clonefile), which blocks dlopen of unsigned libraries. */
	private void removeQuarantine(Path file) {
		if (OS.MacOS != os) return;
		try {
			UserDefinedFileAttributeView view = Files.getFileAttributeView(file, UserDefinedFileAttributeView.class);
			if (null != view && view.list().contains("com.apple.quarantine")) view.delete("com.apple.quarantine");
		} catch (IOException | UnsupportedOperationException ignore) {}
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Legacy mode: flat cache directory, exact content comparison

	/**
	 * Copies the library into the persistent cache when it is missing there or differs from the source
	 * (exact content comparison, so a rebuilt library of equal size/timestamp is detected too).
	 * The cache file is only ever replaced by a fully written and verified sibling temp file.
	 * @return the file to load: the cache file; the source file itself if a changed library cannot be published
	 * (e.g. the old one is mapped by a running instance); a stale cache file only if there is no other choice;
	 * null if the library is unavailable.
	 */
	private Path ensureCachedFile(String fileName, boolean quiet) {
		Path target = legacyCacheDir().resolve(fileName);
		String source = sourceOf(fileName);
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
		if (!quiet) log.warn("doesn't exist: {}", source);
		return null;
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
			removeQuarantine(tmp);
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

	/** Deletes leftovers ({@code *.<pid>-<nanos>.tmp}, {@code *.old-<nanos>}) older than {@link #STALE_MILLIS} in the flat cache directory. */
	private void sweepStale() {
		Path dir = legacyCacheDir();
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

	// ---------------------------------------------------------------------------------------------------------------
	// Public API

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

	/** One line per native file for diagnostics (PLAN B10): {@code <file>: <state>}. */
	public synchronized List<String> report() {
		readManifests();
		List<String> lines = new ArrayList<>();
		lines.add("natives source: " + sourceOf("") + (null == authority ? " (legacy mode, cache " + legacyCacheDir() + ")" : " (bundle " + authority.id + ", cache " + bundleDir + ")"));
		Map<String, String> byFile = new LinkedHashMap<>();
		for (Map.Entry<String, State> e : states.entrySet()) byFile.put(fileNames.getOrDefault(e.getKey(), e.getKey()), e.getValue().name());
		for (String file : loadedFiles) byFile.putIfAbsent(file, State.LOADED.name());
		for (Map.Entry<String, State> e : fileStates.entrySet()) byFile.putIfAbsent(e.getKey(), e.getValue().name());
		Manifest listed = null != expected ? expected : authority;
		if (null != listed) {
			for (String file : listed.sha256.keySet()) lines.add(file + ": " + byFile.getOrDefault(file, "not requested"));
			for (Map.Entry<String, String> e : byFile.entrySet()) if (!listed.sha256.containsKey(e.getKey())) lines.add(e.getKey() + ": " + e.getValue() + " (not in manifest)");
		} else {
			for (Map.Entry<String, String> e : byFile.entrySet()) lines.add(e.getKey() + ": " + e.getValue());
		}
		return lines;
	}

	/**
	 * Loads every file of the manifest (the jar's, else the source's) in manifest order, dependencies first, and
	 * returns the failures ({@code <file>: <reason>}); empty if all natives are loadable. Without a manifest, nothing is
	 * checked and the single entry explains why. Used by {@code --check-natives} (docs/NATIVES.md §4).
	 */
	public synchronized List<String> checkAll() {
		readManifests();
		Manifest listed = null != expected ? expected : authority;
		if (null == listed) return List.of("no natives manifest: nothing to check (legacy mode, source " + sourceOf("") + ")");
		List<String> failures = new ArrayList<>();
		for (String file : listed.sha256.keySet()) {
			Path path = prepareFile(file, false);
			if (null == path) {
				fileStates.put(file, State.MISSING);
				failures.add(file + ": unavailable");
				continue;
			}
			if (loadedFiles.contains(file)) continue;
			if (file.contains("swt-awt")) {
				try { System.loadLibrary("jawt"); } catch (Throwable t) { failures.add(file + ": JDK jawt not loadable: " + t.getMessage()); }
			}
			loadDependencies(file);
			try {
				System.load(path.toString());
				loadedFiles.add(file);
			} catch (UnsatisfiedLinkError e) {
				fileStates.put(file, State.FAILED);
				failures.add(file + ": " + e.getMessage());
			}
		}
		return failures;
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

	/** The explicit location override ({@value #LOCATION_PROPERTY}, then {@value #LOCATION_ENV}), or null. */
	private static String overrideLocation() {
		String override = System.getProperty(LOCATION_PROPERTY);
		if (override == null || override.isBlank()) override = System.getenv(LOCATION_ENV);
		return (override == null || override.isBlank()) ? null : override.trim();
	}

	/**
	 * The natives source root, ending with '/'. Precedence: {@value #LOCATION_PROPERTY} system property,
	 * {@value #LOCATION_ENV} environment variable, {@code location} in {@code /libs.properties}, the directory of the
	 * jar that contains {@code libs.properties} if it has a {@code libs/} subdirectory, then that jar itself
	 * ({@code jar:} URL); "" if none (cache only). Values are a directory path or a {@code file:}/{@code jar:}/{@code http(s):} URL.
	 */
	private String getLocations() {
		String override = overrideLocation();
		if (override != null) return toLocation(override);

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
		if (idx < 0) return "";
		String jar = loc.substring(loc.startsWith("jar:") ? 4 : 0, idx + 4);
		try {
			Path dir = Paths.get(new URI(jar)).toAbsolutePath().getParent();
			if (null != dir && Files.isDirectory(dir.resolve("libs").resolve(basePath).resolve(arch))) return fixBs(dir.toUri().toString());
		} catch (URISyntaxException | IllegalArgumentException | FileSystemNotFoundException ignore) {}
		return loc.substring(0, idx + 5) + '/';
	}

	private static String toLocation(String loc) {
		if (loc.startsWith("file:") || loc.startsWith("jar:") || loc.startsWith("http")) return fixBs(loc);
		return fixBs(Paths.get(loc).toUri().toString()); // toUri() omits the trailing '/' for a missing directory
	}

}
