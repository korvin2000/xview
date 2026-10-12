package org.delafer.xanderView.gui.config;

import java.util.concurrent.atomic.AtomicInteger;

public class ApplInstance {

	public static AtomicInteger openTasks = new AtomicInteger(0);

	public final static String LAST_ENTRY = "_zlst.csv";
	public final static String LAST_ENTRY_DIV = ",";

	public static String lastEntry;

}
