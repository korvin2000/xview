package org.delafer.xanderView.gui;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Per-OS runtime prerequisites that a running JVM cannot give itself (PLAN P5.4). When one is missing, the app starts
 * itself once more with it and this JVM exits with the child's exit code:
 * <ul>
 * <li>macOS: SWT must own the main thread ({@code -XstartOnFirstThread}); the java launcher confirms it with the
 * environment variable {@code JAVA_STARTED_ON_FIRST_THREAD_<pid>=1}.</li>
 * <li>Linux: {@code SWT_AWT} embeds the AWT canvas via XEmbed, i.e. X11. In a Wayland session GTK must use XWayland,
 * which needs {@code GDK_BACKEND=x11} before GTK starts. A user-set {@code GDK_BACKEND} is respected.</li>
 * <li>Windows: SWT 4.527's {@code SWT_AWT} reflects into {@code sun.awt.windows}. {@code java -jar} gets the export from
 * the manifest's {@code Add-Exports}; a classpath launch (IDE, {@code -cp}) without
 * {@code --add-exports=java.desktop/sun.awt.windows=ALL-UNNAMED} is restarted once with it.</li>
 * </ul>
 * The child gets {@code -Dxview.relaunched=true} and never relaunches again; {@code -Dxview.relaunch=false} disables
 * the check (e.g. when a launcher already sets the flags). Must run first in {@code main}, before anything loads AWT
 * or SWT: macOS AWT removes the {@code JAVA_STARTED_ON_FIRST_THREAD_*} variable when it initializes.
 */
final class PlatformLaunch {

	private static final Logger log = LoggerFactory.getLogger(PlatformLaunch.class);

	static final String RELAUNCHED = "xview.relaunched";
	static final String RELAUNCH = "xview.relaunch";

	private PlatformLaunch() {
	}

	/** @return the child's exit code if the app ran in a relaunched JVM, or -1 to continue in this JVM */
	static int relaunchIfNeeded(String[] args) {
		if (Boolean.getBoolean(RELAUNCHED) || "false".equalsIgnoreCase(System.getProperty(RELAUNCH))) return -1;
		String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		List<String> jvmFlags = new ArrayList<>();
		Map<String, String> env = new HashMap<>();
		if (os.contains("mac") || os.contains("darwin")) {
			if ("1".equals(System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + ProcessHandle.current().pid()))) return -1;
			jvmFlags.add("-XstartOnFirstThread");
		} else if (os.contains("linux")) {
			if (!needsX11Backend()) return -1;
			env.put("GDK_BACKEND", "x11");
		} else if (os.contains("windows")) {
			if (!needsAwtWindowsExport()) return -1;
			jvmFlags.add("--add-exports=java.desktop/sun.awt.windows=ALL-UNNAMED");
		} else {
			return -1;
		}
		return relaunch(jvmFlags, env, args);
	}

	/** @return true if {@code java.desktop/sun.awt.windows} exists but is not exported to this (unnamed) module */
	private static boolean needsAwtWindowsExport() {
		Module desktop = ModuleLayer.boot().findModule("java.desktop").orElse(null);
		if (desktop == null || !desktop.getPackages().contains("sun.awt.windows")) return false;
		if (desktop.isExported("sun.awt.windows", PlatformLaunch.class.getModule())) return false;
		log.warn("java.desktop/sun.awt.windows is not exported (classpath launch): restarting with it. "
				+ "Add --add-exports=java.desktop/sun.awt.windows=ALL-UNNAMED to the VM options to skip the restart (and to debug in the same JVM).");
		return true;
	}

	private static boolean needsX11Backend() {
		String backend = System.getenv("GDK_BACKEND");
		if (backend != null && !backend.isBlank()) {
			if (!backend.contains("x11")) log.warn("GDK_BACKEND={}: the embedded image canvas (SWT_AWT, XEmbed) needs x11", backend);
			return false;
		}
		boolean wayland = "wayland".equalsIgnoreCase(System.getenv("XDG_SESSION_TYPE")) || System.getenv("WAYLAND_DISPLAY") != null;
		if (!wayland) return false;
		if (System.getenv("DISPLAY") == null) {
			log.warn("Wayland session without XWayland (DISPLAY is not set): the embedded image canvas (SWT_AWT) needs X11");
			return false;
		}
		return true;
	}

	private static int relaunch(List<String> jvmFlags, Map<String, String> env, String[] args) {
		String java = ProcessHandle.current().info().command()
				.orElse(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
		List<String> command = new ArrayList<>();
		command.add(java);
		command.addAll(jvmFlags);
		for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			// a debugger port is already taken by this JVM; the guard property is set below
			if (arg.startsWith("-agentlib:jdwp") || arg.startsWith("-Xrunjdwp") || "-Xdebug".equals(arg)
					|| arg.startsWith("-D" + RELAUNCHED)) continue;
			command.add(arg);
		}
		command.add("-D" + RELAUNCHED + "=true");
		// -jar starts become -cp starts: the manifest no longer applies (a needed --add-exports is already in jvmFlags)
		command.add("--enable-native-access=ALL-UNNAMED");
		command.add("-cp");
		command.add(System.getProperty("java.class.path"));
		command.add(MainEntryPoint.class.getName());
		Collections.addAll(command, args);

		ProcessBuilder builder = new ProcessBuilder(command).inheritIO(); // same working directory and environment
		builder.environment().putAll(env);
		log.info("restarting with {} {}", jvmFlags, env);
		Process child;
		try {
			child = builder.start();
		} catch (IOException e) {
			log.warn("relaunch failed, continuing without {} {}", jvmFlags, env, e);
			return -1;
		}
		Runtime.getRuntime().addShutdownHook(new Thread(child::destroy, "relaunch-cleanup")); // Ctrl-C: don't orphan the child
		try {
			return child.waitFor();
		} catch (InterruptedException e) {
			child.destroy();
			Thread.currentThread().interrupt();
			return 1;
		}
	}
}
