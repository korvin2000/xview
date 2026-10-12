package net.j7.commons.io;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import net.j7.commons.strings.StringUtils;


public class SysInfo {

	private static final String PROPERTY_VM_NAME = "java.vm.name";

	public enum ArchVM {x32, x64, unknown};

    public enum OSFamily {Windows, Unix, BeOS, OS2, OpenVMS, Other};

	public enum OS {
		Windows9xME(OSFamily.Windows),
		WindowsNT5(OSFamily.Windows),
		Windows(OSFamily.Windows),
		MacOS(OSFamily.Unix),
		Linux(OSFamily.Unix),
		Android(OSFamily.Unix),
		Solaris (OSFamily.Unix),
		Unknown(OSFamily.Other),
		HPUX(OSFamily.Unix),
		AIX(OSFamily.Unix),
		UnixGeneric(OSFamily.Unix)
		;

	OSFamily family;

	private OS(OSFamily family) {
		this.family = family;
	}

	public OSFamily family() {
		return this.family;
	}

	};
	/**
	 * Lazy-loaded Singleton, by Bill Pugh *.
	 */
	private static final class Holder {
		/** The Constant INSTANCE. */
		private final static transient SysInfo INSTANCE = new SysInfo();
	}

	/**
	 * Gets the single instance of ResourcesDR.
	 *
	 * @return single instance of ResourcesDR
	 */
	public static final SysInfo instance() {
		return Holder.INSTANCE;
	}

	private String osName;
	private String javaArch;
	private String osArch;
	private OS osEnum;
	private String userHome;
	private Map<String, String> appData;

	public String osName() {
		if (null == osName) {
			osName = System.getProperty("os.name");
		}
		return osName;
	}


	public String archOS() {
		if (null == osArch) {
			osArch = System.getProperty ("os.arch");
		}
		return osArch;
	}

	private String archJava() {
		if (null == javaArch) {
			String dataModel =  System.getProperty ("sun.arch.data.model"); //$NON-NLS-1$
			if (StringUtils.empty(dataModel)) dataModel = System.getProperty ("com.ibm.vm.bitmode"); //$NON-NLS-1$
			if (StringUtils.empty(dataModel)) dataModel = archOS(); //$NON-NLS-1$
			javaArch = dataModel;
		}
		return javaArch;
	}

	public String userHome() {
		if (null == userHome) {
			userHome = System.getProperty("user.home", ".");
		}
		return userHome;
	}


	public ArchVM getDataModel() {
		return archJava().contains("64") ? ArchVM.x64 : ArchVM.x32;

	}

	public OS getOS() {
		String osName = osName();
		if (null == osEnum) {
			osName = osName.toLowerCase();
			OS platform = null;
			if (osName.contains("linux")) platform = OS.Linux;
			else if (osName.contains("windows")) platform = OS.Windows;
			else if (osName.contains("mac os") || osName.contains("macos") || osName.contains("darwin")) platform = OS.MacOS;
			else if ((osName.contains("solaris")) || (osName.contains("sunos") || osName.contains("sun os"))) platform = OS.Solaris;
			else if (osName.contains("hp-ux")) platform = OS.HPUX;
			else if (osName.contains("aix")) platform = OS.AIX;
			else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix") || osName.contains("freebsd") || osName.contains("mpe/ix") || osName.contains("irix") || osName.contains("hp-ux")) platform = OS.UnixGeneric;
			else platform = OS.Unknown;
			osEnum = platform;
		}
		return osEnum;

	}


	public String getAppDataDir(String appName) {

		String dir = appData.get(appName);

		if (null == dir) {
			LazyValues ret = getAppDataDirInt();
			for (int i = 0; i < ret.size(); i++) {
				String path = ret.get(i);
				if (!StringUtils.isEmpty(path) && checkWritePermission(path)) {
					dir = ret.completePath(path, appName);
					break;
				}
			}
			appData.put(appName, dir);
		}


		return dir;
	}

	private LazyValues getAppDataDirInt() {
	    OS os = getOS();

	    switch (os) {
		case Windows:
	    	return new LazyValues(4) {
				public String get(int x) {
					switch(x){
					default:
					case 1: return System.getenv("APPDATA");
					case 2: return userHome() + "\\Local Settings\\ApplicationData";
					case 3: return System.getenv("LOCALAPPDATA");
					case 4: return System.getProperty("java.io.tmpdir");
					}
				}
				String completePath(String basePath, String appName) {
					return basePath + "\\" + appName;
				}
	    	};
		case MacOS:
	    	return new LazyValues(3) {
				String get(int x) {
					switch(x){
					default:
					case 1: return userHome() + "/Library/Application Support";
					case 2: return System.getenv("USER") + "/Library";
					case 3: return System.getProperty("java.io.tmpdir");
					}
				}
				String completePath(String basePath, String appName) {
					return basePath+"/"+appName;
				}
	    	};
		case Solaris:
		case Linux:
	    	return new LazyValues(7) {
				String get(int x) {
					switch(x){
					default:
					case 1: return userHome() + "/.local/share";
					case 2: return userHome() + "/.config";
					case 3: return userHome() ;
					case 4: return System.getProperty("user.dir") + "/config";
					case 5: return "/var/local";
					case 6: return "/usr/share";
					case 7: return System.getProperty("java.io.tmpdir"); //return "/var/tmp";
					}
				}
				String completePath(String basePath, String appName) {
					return basePath + "/." + appName;
				}
	    	};
		default:
	    	return new LazyValues(3) {
				String get(int x) {
					switch(x){
					default:
					case 1: return System.getProperty("user.dir");
					case 2: return userHome() + "/.local/share";
					case 3: return System.getProperty("java.io.tmpdir");
					}
				}
				String completePath(String basePath, String appName) {
					return basePath+ File.pathSeparator +appName;
				}
	    	};
		}

	}


	private SysInfo() {
		appData = new HashMap<String, String>(4);
	}

	@SuppressWarnings("unused")
	private static abstract class LazyValues {
		int total;
		LazyValues(int total) {
			this.total = total;
		}
		int size() {return this.total;}
		abstract String get(int x);
		abstract String completePath(String basePath, String appName);

	}

	public static void main(String[] args) {
		System.out.println(System.getProperty(PROPERTY_VM_NAME));
		System.out.println(System.getProperty("os.arch"));
		System.out.println(SysInfo.instance().archJava());
		System.out.println(SysInfo.instance().getAppDataDir("x"));
		System.out.println(SysInfo.instance().getAppDataDir("y"));
		System.out.println(SysInfo.instance().getAppDataDir("x"));
		System.out.println(SysInfo.instance().getAppDataDir(""));
		System.out.println(SysInfo.instance().getAppDataDir(""));

			}

	public boolean checkWritePermission(String dir) {
		if (Files.isWritable(FileSystems.getDefault().getPath(dir)))
			return true;
		else
			return false;

//		(1)
//		File f = new File(dir);
//		return f.canWrite() ? true : false;

//		(2)
//		return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
//			public Boolean run() {
//				try {
//					FilePermission perm = new FilePermission(dir, "write");
//					AccessController.checkPermission(perm);
////				AccessController.checkPermission(new FilePermission(dir, "read"));
//				return true;
//			} catch (SecurityException e) {
//				return false;
//			}
//			}
//		});
	}

}
