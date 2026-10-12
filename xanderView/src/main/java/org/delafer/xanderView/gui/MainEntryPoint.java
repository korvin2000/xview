package org.delafer.xanderView.gui;

import net.j7.commons.jni.LibraryLoader;
import net.j7.commons.utils.Metrics;

import org.delafer.xanderView.file.CopyService;
import org.delafer.xanderView.general.Shutdown;
import org.delafer.xanderView.gui.config.OrientationStore;
import org.delafer.xanderView.gui.helpers.ImageRepository;

import java.net.URISyntaxException;
import java.net.URL;

public class MainEntryPoint {


	public static void main(String[] args) {
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
//		ResizerOpenCV.instance();
		try {
			OrientationStore.instance().load();
		} catch (Exception e) {
			e.printStackTrace();
		}
		CopyService.instance();
	}

}
