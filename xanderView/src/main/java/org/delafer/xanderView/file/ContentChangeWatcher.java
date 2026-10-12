package org.delafer.xanderView.file;

import java.io.IOException;

import org.delafer.xanderView.interfaces.IAbstractReader;
import org.delafer.xanderView.interfaces.IAbstractReader.FileEvent;

public interface ContentChangeWatcher {
	void onEvent(FileEvent type, Object identifier) throws IOException;
}