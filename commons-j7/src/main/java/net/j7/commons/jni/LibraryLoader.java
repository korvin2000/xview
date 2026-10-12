package net.j7.commons.jni;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import net.j7.commons.base.NotNull;
import net.j7.commons.io.*;
import net.j7.commons.io.SysInfo.ArchVM;
import net.j7.commons.io.SysInfo.OS;
import net.j7.commons.io.SysInfo.OSFamily;
import net.j7.commons.streams.Streams;


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
	private Map<String, Boolean> loaded;
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
		loaded = new HashMap<String, Boolean>();
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

	private boolean load(String name, boolean load) {
//		System.out.println(name);
		Boolean state = loaded.get(name);
		if (null == state) {
			state = Boolean.valueOf(load0(name, load));
			loaded.put(name, state);
		}
		return state;
	}

	private static String appName = "xanderView";

	private InputStream fromURL(String url) {
        try {
			System.out.println("extracting dll: "+url);
            return new URL(url).openStream();
        } catch (IOException e) {
			return getClass().getResourceAsStream(url);
        }
    }

	private boolean load0(String name, boolean load) {
		FilePath path = getFSPath(appName, name);
		String pathTxt = path.build();
		File fLib = new File(pathTxt);
//		boolean load = true;
		if (!fLib.exists()) {
			InputStream is = fromURL(getJARPath(name, getLibPath()));
			if (null != is) {
//				System.out.println("exists in jar: "+getJARPath(name));
				path.forceExists();
				path.build();
				copyLibrary(fLib, is);
				chmod ("755", pathTxt);
//				System.out.println(pathTxt);
			} else {
				load = false;
				System.err.println("doesn't exits: "+getJARPath(name, getLibPath()));
			}

		}
		if (load) {
			System.load(pathTxt);
		} else {
			String locDir = fLib.getParent();
			System.setProperty("jna.platform.library.path", locDir);
			System.setProperty("jna.library.path", locDir);
		}
		return load;
 	}


	private void copyLibrary(File fLib, InputStream is) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(fLib);
			byte[] buffer = new byte[2048];
		    int bytesRead;
		    while ((bytesRead = is.read(buffer)) > 0) {
		        out.write(buffer, 0, bytesRead);
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		Streams.close(out);
		Streams.close(is);
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

