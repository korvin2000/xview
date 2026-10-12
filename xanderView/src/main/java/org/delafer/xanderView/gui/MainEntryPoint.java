package org.delafer.xanderView.gui;

import net.j7.commons.jni.LibraryLoader;
import net.j7.commons.utils.Metrics;

import org.delafer.xanderView.file.CopyService;
import org.delafer.xanderView.general.Shutdown;
import org.delafer.xanderView.gui.config.OrientationStore;
import org.delafer.xanderView.gui.helpers.ImageRepository;

import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainEntryPoint {

	private static final Logger log = LoggerFactory.getLogger(MainEntryPoint.class);


	public static void main(String[] args) {
		// headless diagnostics (docs/NATIVES.md §4, PLAN B10): load every native of the bundle manifest and exit
		if (args.length > 0 && NativeCheck.OPTION.equals(args[0])) System.exit(NativeCheck.run());

		// first: a missing per-OS prerequisite (macOS main thread, X11 on Wayland) restarts the app (PLAN P5.4)
		int relaunched = PlatformLaunch.relaunchIfNeeded(args);
		if (relaunched >= 0) System.exit(relaunched);

		// stock SWT 3.135 autoscales per monitor by default; the vendored 4.527 did not and the AWT side is pinned to 1x,
		// so keep SWT unscaled too (mixed SWT points / AWT pixels would misplace the embedded canvas and change the
		// decode denominator). SWT 3.135 refuses autoScale=false while monitor-specific scaling is on, so that is switched
		// off as well (the JDK launcher already runs the process PerMonitorV2, so coordinates stay raw pixels).
		// Overridable with -Dswt.autoScale=… -Dswt.autoScale.updateOnRuntime=… for experiments.
		if (null == System.getProperty("swt.autoScale")) System.setProperty("swt.autoScale", "false");
		if (null == System.getProperty("swt.autoScale.updateOnRuntime")) System.setProperty("swt.autoScale.updateOnRuntime", "false");
		System.setProperty("sun.java2d.dpiaware", "false");
		System.setProperty("prism.allowhidpi", "false");
		System.setProperty("sun.java2d.win.uiScaleX", "1");
		System.setProperty("sun.java2d.win.uiScaleY", "1");
		System.setProperty("sun.java2d.uiScale", "1");
		System.setProperty("sun.java2d.uiScale.enabled", "false");

//		URL url  = LibraryLoader.class.getResource("/dummy.cl");
//		System.out.println(">>>>"+url);
//
//		URL libUrl = LibraryLoader.class.getResource("/libs.properties");
//		System.out.println("1:"+libUrl.toExternalForm());
//		try {
//			System.out.println("2:"+libUrl.toURI().toString());
//		} catch (URISyntaxException e) {
//			throw new RuntimeException(e);
//		}


		Shutdown.addHook();
		String toOpen = args.length > 0 ? args[0] : null;
//		if (StringUtils.isEmpty(toOpen)) {
//			System.out.println("Please specify an image name or directory to start jpeg viewer");
//			System.exit(0);
//		}
		Metrics.enable(false);

		initializeApp();


		MainWindow gui = new MainWindow();
		gui.open(toOpen);
		System.exit(0);

	}

	private static void initializeApp() {
		ImageRepository.loadImages();
		try {
			OrientationStore.instance().load();
		} catch (Exception e) {
			log.error("cannot load the saved view state", e);
		}
		CopyService.instance();
	}

}
