package net.j7.commons.io;

import java.io.File;

import net.j7.commons.io.SysInfo.OS;


public class FilePath {


	private static final Character WIN_SEP = Character.valueOf(FileUtils.WINDOWS_SEPARATOR);
	private static final Character UNIX_SEP = Character.valueOf(FileUtils.UNIX_SEPARATOR);

	public enum PathType {UserHome, Temp, ApplicationData, AppDataLocal, UserDir};

	StringBuilder sb;
	boolean autocreate;
	boolean filemode;
	Boolean separatorAtEnd;

	private FilePath() {
		sb = new StringBuilder();
		autocreate = false;
		filemode = false;
		separatorAtEnd = false;
	}


	public static FilePath as() {
		return new FilePath();
	}

	public FilePath div(boolean flag) {
		separatorAtEnd = Boolean.valueOf(flag);
		return this;
	}

	public FilePath forceExists() {
		autocreate = true;
		return this;
	}

	public String build() {

		if (!filemode && separatorAtEnd != null) {
			if (isPathDiv(lastCh(sb)) != separatorAtEnd.booleanValue()) {
				if (separatorAtEnd)
					sb.append(FileUtils.SYSTEM_SEPARATOR);
				else
					sb.deleteCharAt(sb.length()-1);
			}
		}

		String ret =  FileUtils.convertToSystemPath(sb.toString());

		if (autocreate) {
			File path = new File(ret);
			if (!path.exists()) {
				File mkPath = filemode ? path.getParentFile() : path;
				if (null != mkPath) {
					mkPath.mkdirs();
				}

				if (filemode)
					createNewFile(path);
			}
		}

		return ret;
	}


	private void createNewFile(File path)  {
		try {
			path.createNewFile();
		} catch (Exception e) {
			// silent operation
		}

	}

	public FilePath dir(PathType type) {
		sb.setLength(0);
		switch (type) {
		case ApplicationData:
			return dir(SysInfo.instance().getAppDataDir(""));
		case UserHome:
			return dir(System.getProperty("user.home"));
		case Temp:
			return dir(System.getProperty("java.io.tmpdir"));
		case AppDataLocal:
			SysInfo si = SysInfo.instance();
			return dir(OS.Windows.equals(si.getOS()) ? System.getenv("LOCALAPPDATA") : SysInfo.instance().getAppDataDir(""));
		case UserDir:
			return dir(System.getProperty("user.dir"));
		default:
			throw new UnsupportedOperationException("not implemented: "+type);
		}

	}

	public FilePath dir(String path) {
		if (filemode) throw new RuntimeException("Wrong operation!");
		checkPath(path);
		sb.append(path);
		return this;
	}

	public FilePath file(String path) {
		checkPath(path);
		sb.append(path);
		filemode = true;
		return this;
	}


	private void checkPath(String path) {
		if (notEmpty(sb)) {
			if (isPathDiv(lastCh(sb)) && isPathDiv(firstCh(path))) {
				sb.deleteCharAt(sb.length()-1);
			} else
			if (!isPathDiv(lastCh(sb)) && !isPathDiv(firstCh(path))) {
					sb.append(FileUtils.SYSTEM_SEPARATOR);
			}
		}
	}


	private Character lastCh(CharSequence cs) {
		return notEmpty(cs)  ? cs.charAt(cs.length()-1) : null;
	}

	private Character firstCh(CharSequence cs) {
		return notEmpty(cs) ? cs.charAt(0) : null;
	}

	private boolean isPathDiv(Character ch) {
		return WIN_SEP.equals(ch) || UNIX_SEP.equals(ch);
	}

	private boolean notEmpty(CharSequence cs) {
		return cs != null && cs.length() > 0;
	}


	public static void main(String[] args) {
		String s = FilePath.as().dir(PathType.ApplicationData).dir("xanderView").file("xander.properties").forceExists().build();
		System.out.println(s);
		String s2 = FilePath.as().dir(PathType.ApplicationData).dir("xanderView").div(true).build();
		System.out.println(s2);
	}


}
