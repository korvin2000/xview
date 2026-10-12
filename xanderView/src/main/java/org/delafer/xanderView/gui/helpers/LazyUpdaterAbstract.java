package org.delafer.xanderView.gui.helpers;


public final class LazyUpdaterAbstract extends Thread {

	volatile long start;
	final static long interval =750;
	private Runnable task;

	/**
	 *
	 */
	public LazyUpdaterAbstract() {
		super("");
		this.setDaemon(true);
		this.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY)>>1);

	}

	static LazyUpdaterAbstract updater;
	static final Object obj = new Object();

	public static void doTask(Runnable task) {
		synchronized (obj) {
			if (null == updater) {
				updater = new LazyUpdaterAbstract();
				updater.start();
			}	
		}
		updater.update(task);
	}

	public synchronized void update(Runnable task) {
		this.task = task;
		if (!this.isAlive()) this.start();
		this.start = System.currentTimeMillis();

	}



	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		while (System.currentTimeMillis() - start < interval) {
			try {
				sleep(50);
				Thread.yield();
			} catch (InterruptedException e) {}
		}
		task.run();
		LazyUpdaterAbstract.updater = null;
	}


}
