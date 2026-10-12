package org.delafer.xanderView.general;

import java.io.IOException;
import java.util.Scanner;

import org.delafer.xanderView.common.CopyLogger;
import org.delafer.xanderView.file.CopyService;
import org.delafer.xanderView.gui.config.ApplConfiguration;
import org.delafer.xanderView.gui.config.ApplInstance;
import org.delafer.xanderView.gui.config.OrientationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Shutdown {

	private static final Logger log = LoggerFactory.getLogger(Shutdown.class);

	public static void addHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				log.info("Terminating application...");
				ApplConfiguration.instance().save();
				try {
					OrientationStore.instance().save();
					CopyLogger.flushIfNeeded();
				} catch (IOException e) {
					log.error("Can't save orientation data or flush copy log", e);
				}
				int i = 0;
				while (ApplInstance.openTasks.get() > 0 || i < 6) {
					try {
						i++;
						sleep(250);Thread.yield();
					} catch (InterruptedException e) {}
				}
				CopyService.closeAll();
				Shutdown.exit();
			}
		});
	}

	private static void clearKeyboard() {
		boolean found = false;
		long start = System.currentTimeMillis();
		do {
			try {

				if (System.in.available()>0) {
					byte[] buf = new byte[System.in.available()];
					System.in.read(buf);
					found = true;
				}
			} catch (IOException e) {
				found = false;
			}
		} while (found && (System.currentTimeMillis() - start)<500L);
		try {
			System.in.reset();
		} catch (IOException ignore) {}
	}

	public static void exit() {
		try {
		Thread cur = Thread.currentThread();
		int tCnt = Thread.activeCount();
		if (tCnt > 0) {
			Thread[] threads = new Thread[tCnt];
			Thread.enumerate(threads);
			for (Thread thread : threads) {
				try {
					if (thread != cur)
						thread.interrupt();
				} catch (Throwable e) {}
			}
		}
		log.info("{} ACTIVE THREADS STOPPED", tCnt);
		clearKeyboard();
//		cur.interrupt();

	} catch (Exception e) {
		log.error("Error while terminating application", e);
	}

	}
}
