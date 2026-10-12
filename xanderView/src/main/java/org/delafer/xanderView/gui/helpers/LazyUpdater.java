package org.delafer.xanderView.gui.helpers;

import java.awt.EventQueue;

import org.delafer.xanderView.gui.ImageCanvas;

public class LazyUpdater extends Thread {

	volatile long start;
	final static long interval = 100;
	private ImageCanvas panel;

	/**
	 *
	 */
	public LazyUpdater(ImageCanvas panel) {
		super("");
		this.panel = panel;
		this.setDaemon(true);
		this.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY)>>1);
	}

	public synchronized void update() {

		if (!this.isAlive()) this.start();

		this.start = System.currentTimeMillis();

	}



	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		while (System.currentTimeMillis() - start < interval) {
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {}
		}
		drawIt();
	}

	/**
	 *
	 */
	private final void drawIt() {
		if (null == panel) return ;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (panel.preRenderImage()) panel.showImage();
			}
		});
	}

}
