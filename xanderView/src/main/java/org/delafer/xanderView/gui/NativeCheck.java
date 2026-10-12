package org.delafer.xanderView.gui;

import java.util.List;

import net.j7.commons.jni.LibraryLoader;

/**
 * {@code java -jar xanderview.jar --check-natives}: loads every native listed in the bundle manifest
 * ({@code libs/<os>/<arch>/natives.sha256}, dependencies first) without starting the GUI, prints one line per native
 * and exits 0 when all are loadable, 1 otherwise (docs/NATIVES.md §4, PLAN B10). Used by the CI probe.
 */
final class NativeCheck {

	static final String OPTION = "--check-natives";

	private NativeCheck() {}

	static int run() {
		LibraryLoader loader = LibraryLoader.instance();
		List<String> failures;
		try {
			failures = loader.checkAll();
		} catch (RuntimeException | LinkageError e) {
			System.out.println("natives check aborted: " + e);
			return 1;
		}
		for (String line : loader.report()) System.out.println(line);
		for (String failure : failures) System.out.println("FAILED " + failure);
		boolean ok = failures.isEmpty();
		System.out.println(ok ? "natives: OK" : "natives: " + failures.size() + " problem(s)");
		return ok ? 0 : 1;
	}
}
