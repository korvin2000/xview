/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.jar.*;

import net.j7.commons.jni.LibraryLoader; // xview-patch: upstream SWT 3.135.0 Library.java (same for gtk and cocoa), loadLibrary -> LibraryLoader

public class Library {

	/* SWT Version - Mmmm (M=major, mmm=minor) */

	/**
	 * SWT Major version number (must be >= 0)
	 */
	static int MAJOR_VERSION = 4;

	/**
	 * SWT Minor version number (must be in the range 0..999)
	 */
	static int MINOR_VERSION = 974;

	/**
	 * SWT revision number (must be >= 0)
	 */
	static int REVISION = 10;

	/**
	 * The JAVA and SWT versions
	 */
	public static final int JAVA_VERSION, SWT_VERSION;
	public static final String USER_HOME;

	static final String SEPARATOR;
	static final String DELIMITER;

	static final String JAVA_LIB_PATH = "java.library.path";
	static final String SWT_LIB_PATH = "swt.library.path";

	static final String SUFFIX_64 = "-64";	//$NON-NLS-1$
	static final String SWT_LIB_DIR;

static {
	DELIMITER = System.lineSeparator();
	SEPARATOR = File.separator;
	USER_HOME = System.getProperty ("user.home");
	SWT_LIB_DIR = ".swt" + SEPARATOR + "lib" + SEPARATOR + os() + SEPARATOR + arch(); //$NON-NLS-1$ $NON-NLS-2$
	JAVA_VERSION = parseVersion(System.getProperty("java.version")); //$NON-NLS-1$
	SWT_VERSION = SWT_VERSION(MAJOR_VERSION, MINOR_VERSION);
}

static String arch() {
	String osArch = System.getProperty("os.arch"); //$NON-NLS-1$
	if (osArch.equals ("amd64")) return "x86_64"; //$NON-NLS-1$ $NON-NLS-2$
	return osArch;
}

static String os() {
	String osName = System.getProperty("os.name"); //$NON-NLS-1$
	if (osName.equals ("Linux")) return "linux"; //$NON-NLS-1$ $NON-NLS-2$
	if (osName.equals ("Mac OS X")) return "macosx"; //$NON-NLS-1$ $NON-NLS-2$
	if (osName.startsWith ("Win")) return "win32"; //$NON-NLS-1$ $NON-NLS-2$
	return osName;
}

/* Use method instead of in-lined constants to avoid compiler warnings */
static long longConst() {
	return 0x1FFFFFFFFL;
}

static int parseVersion(String version) {
	if (version == null) return 0;
	int major = 0, minor = 0, micro = 0;
	int length = version.length(), index = 0, start = 0;
	while (index < length && Character.isDigit(version.charAt(index))) index++;
	try {
		if (start < length) major = Integer.parseInt(version.substring(start, index));
	} catch (NumberFormatException e) {}
	start = ++index;
	while (index < length && Character.isDigit(version.charAt(index))) index++;
	try {
		if (start < length) minor = Integer.parseInt(version.substring(start, index));
	} catch (NumberFormatException e) {}
	start = ++index;
	while (index < length && Character.isDigit(version.charAt(index))) index++;
	try {
		if (start < length) micro = Integer.parseInt(version.substring(start, index));
	} catch (NumberFormatException e) {}
	return JAVA_VERSION(major, minor, micro);
}

/**
 * Returns the Java version number as an integer.
 *
 * @return the version
 */
public static int JAVA_VERSION (int major, int minor, int micro) {
	return (major << 16) + (minor << 8) + micro;
}

/**
 * Returns the SWT version number as an integer.
 *
 * @return the version
 */
public static int SWT_VERSION (int major, int minor) {
	return major * 1000 + minor;
}

static boolean isLoadable () {
	return isLoadable(false);
}

static boolean isLoadable (boolean ignoreMissingManifest) {
	URL url = Platform.class.getClassLoader ().getResource ("org/eclipse/swt/internal/Library.class"); //$NON-NLS-1$
	if (!url.getProtocol ().equals ("jar")) { //$NON-NLS-1$
		/* SWT is presumably running in a development environment */
		return true;
	}

	Attributes attributes = null;
	try {
		URLConnection connection = url.openConnection();
		if (!(connection instanceof JarURLConnection jc)) {
			/* should never happen for a "jar:" url */
			return false;
		}
		attributes = jc.getMainAttributes();
	} catch (IOException e) {
		/* should never happen for a valid SWT jar with the expected manifest values */
		return false;
	}

	String os = os ();
	String arch = arch ();
	String manifestOS = attributes.getValue ("SWT-OS"); //$NON-NLS-1$
	String manifestArch = attributes.getValue ("SWT-Arch"); //$NON-NLS-1$
	if (arch.equals (manifestArch) && os.equals (manifestOS)) {
		return true;
	}
	if (manifestArch == null && manifestOS == null && ignoreMissingManifest) {
		return true;
	}

	return false;
}

/**
 * Loads the shared library that matches the version of the
 * Java code which is currently running.  SWT shared libraries
 * follow an encoding scheme where the major, minor and revision
 * numbers are embedded in the library name and this along with
 * <code>name</code> is used to load the library.  If this fails,
 * <code>name</code> is used in another attempt to load the library,
 * this time ignoring the SWT version encoding scheme.
 *
 * @param name the name of the library to load
 */
public static void loadLibrary (String name) {
	loadLibrary (name, true);
}

/**
 * Loads the shared library that matches the version of the
 * Java code which is currently running.  SWT shared libraries
 * follow an encoding scheme where the major, minor and revision
 * numbers are embedded in the library name and this along with
 * <code>name</code> is used to load the library.  If this fails,
 * <code>name</code> is used in another attempt to load the library,
 * this time ignoring the SWT version encoding scheme.
 *
 * @param name the name of the library to load
 * @param mapName true if the name should be mapped, false otherwise
 */
public static void loadLibrary (String name, boolean mapName) {
	String prop = System.getProperty ("sun.arch.data.model"); //$NON-NLS-1$
	if (prop == null) prop = System.getProperty ("com.ibm.vm.bitmode"); //$NON-NLS-1$
	if (prop != null) {
		if ("32".equals (prop)) { //$NON-NLS-1$
			throw new UnsatisfiedLinkError ("Cannot load 64-bit SWT libraries on 32-bit JVM"); //$NON-NLS-1$
		}
	}

	// xview-patch: natives come only from net.j7.commons.jni.LibraryLoader (libs/<os>/<arch>/ -> per-user cache);
	// java.library.path, swt.library.path, ~/.swt and extraction from the jar are not used: upstream's load/extract/
	// extractResource/chmod helpers are removed here, and the stock natives and Library classes are excluded when the
	// stock SWT jar is unpacked (thirddep-j7/pom.xml). The candidate stems are the upstream ones (versioned first);
	// LibraryLoader adds the platform's "lib" prefix and extension (libswt-pi3-gtk-4974r10.so, libswt-pi-cocoa-4974r10.jnilib).
	String[] stems = mapName
		? new String[] {name + "-" + Platform.PLATFORM + "-" + getVersionString (), name + "-" + Platform.PLATFORM, name}
		: new String[] {name};
	java.util.List<Throwable> failures = new java.util.ArrayList<> ();
	for (String stem : stems) {
		try {
			if (LibraryLoader.tryLoadLibrary (stem)) return;
		} catch (UnsatisfiedLinkError e) {
			failures.add (e); // present but not loadable (wrong arch, missing dependency): try the next stem, like upstream
		}
	}
	/* Failed to find the library; callers such as gtk OS.<clinit> rely on this error for their fallbacks */
	UnsatisfiedLinkError error = new UnsatisfiedLinkError ("Could not load SWT library " + name + " via LibraryLoader (tried " + String.join (", ", stems) + ")"); //$NON-NLS-1$
	for (Throwable failure : failures) error.addSuppressed (failure);
	throw error;
}

static String mapLibraryName (String libName) {
	return mapLibraryName(libName, true);
}

static String mapLibraryName (String libName, boolean replaceDylib) {
	/* SWT libraries in the Macintosh use the extension .jnilib but the some VMs map to .dylib. */
	libName = System.mapLibraryName (libName);
	String ext = ".dylib"; //$NON-NLS-1$
	if (libName.endsWith(ext) && replaceDylib) {
		libName = libName.substring(0, libName.length() - ext.length()) + ".jnilib"; //$NON-NLS-1$
	}
	return libName;
}

/**
 * @return String Combined SWT version like 4826
 */
public static String getVersionString () {
	String version = System.getProperty ("swt.version"); //$NON-NLS-1$
	if (version == null) {
		version = "" + MAJOR_VERSION; //$NON-NLS-1$
		/* Force 3 digits in minor version number */
		if (MINOR_VERSION < 10) {
			version += "00"; //$NON-NLS-1$
		} else {
			if (MINOR_VERSION < 100) version += "0"; //$NON-NLS-1$
		}
		version += MINOR_VERSION;
		/* "r" followed by respective revision version starting with zero(0) */
		version += "r" + REVISION; //$NON-NLS-1$
	}
	return version;
}

}
