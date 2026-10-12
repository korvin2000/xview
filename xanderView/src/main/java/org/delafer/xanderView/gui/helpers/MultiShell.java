package org.delafer.xanderView.gui.helpers;

import static org.eclipse.swt.SWT.*;

import java.util.ArrayList;
import java.util.List;

import net.j7.commons.types.DoubleValue;

import org.delafer.xanderView.gui.config.ApplConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

public class MultiShell  {


	Shell wndShell;
	Shell fullscrShell;

	public static volatile MultiShell shell;


	public Shell fsShell() {
		return fullscrShell;
	}

	boolean fullscreen = false;


	private List<DoubleValue<Integer, Listener>> listeners;

	public Shell wndShell() {
		return wndShell;
	}

	public MultiShell(Display display, int style) {
		fullscreen = ApplConfiguration.instance().getBoolean(ApplConfiguration.CFG_FULLSCREEN);
		listeners = new ArrayList<DoubleValue<Integer,Listener>>();
		wndShell = new Shell(display,  NO_REDRAW_RESIZE | NO_BACKGROUND | APPLICATION_MODAL | NO_SCROLL | DOUBLE_BUFFERED  | SWT.SHELL_TRIM  );
		//fullscrShell = new Shell(wndShell,  ON_TOP | NO_REDRAW_RESIZE | NO_BACKGROUND | APPLICATION_MODAL | NO_SCROLL | DOUBLE_BUFFERED  | SWT.NO_TRIM);
		fullscrShell = new Shell(wndShell,  NO_REDRAW_RESIZE | NO_BACKGROUND | APPLICATION_MODAL | NO_SCROLL | DOUBLE_BUFFERED  | SWT.NO_TRIM);
		fullscrShell.setFullScreen(true);
		MultiShell.shell = this;
		addIcons();
		updateInfo();

	}

	public void updateInfo() {
		wndShell.setText("COPY DIR = "+ApplConfiguration.instance().getCopyDir());
	}

	public void addIcons() {
		Image img32 = ImageRepository.getImage("large_icon");
//		Image img16 = ImageRepository.getImage("small_icon");
		wndShell.setImages(new Image[] { img32});
	}

	public void setLayout(FillLayout layout) {
		wndShell.setLayout(layout);
		fullscrShell.setLayout(layout);
	}

	public Shell active() {
		return fullscreen ? fullscrShell : wndShell;
	}

	public void setSize(Point shellSize) {
		wndShell.setSize(shellSize);

	}

	public void addListener(int move, Listener listener) {
		listeners.add(new DoubleValue<Integer, Listener>(move, listener));
		active().addListener(move, listener);
	}

	public void open() {
//		wndShell.open();
		active().open();

	}

	public Rectangle getBounds() {
		return active().getBounds();
	}

	public void setLocation(int x, int y) {
		active().setLocation(x, y);

	}

	public boolean getFullScreen() {
		return this.fullscreen;
	}

	public void setMaximized(boolean isFullScreen) {
		active().setMaximized(isFullScreen);
	}


	public void uiUpdate(boolean sendEvent) {
		active().setModified(true);
		active().redraw();
		active().layout(true);
		if (sendEvent) sendResized();
	}

	public void sendResized() {
		active().notifyListeners(SWT.Resize, getResizeEvent());
	}

	public Event getResizeEvent() {
		Event event = new Event();
		event.type = SWT.Resize;
		event.doit = true;
		event.display = Display.getCurrent();
		event.time = OS.GetMessageTime();
		event.widget = active();
		event.item = event.widget;
		return event;
	}

	public void setModified(boolean b) {
		active().setModified(b);

	}

	public Rectangle getClientArea() {
		return active().getClientArea();
	}

	public void close() {
		active().close();

	}


	public void setFullScreen(boolean isFullScreen) {
		ApplConfiguration.instance().set(ApplConfiguration.CFG_FULLSCREEN, Boolean.toString(isFullScreen));
		fullscreenConditional(isFullScreen);
		fullscreenMandatory();

	}

	public void fullscreenMandatory() {
		fullscrShell.setVisible(fullscreen);
		active().setActive();
		active().setFocus();
		active().setMaximized (fullscreen);
	}

	private void fullscreenConditional(boolean isFullScreen) {
		Point loc = active().getLocation();
		for (DoubleValue<Integer, Listener> lst : listeners) {
			active().removeListener(lst.getOne(), lst.getTwo());
		}

		fullscreen = isFullScreen;

		fullscrShell.setVisible(isFullScreen);
		wndShell.setVisible(!isFullScreen);

		for (DoubleValue<Integer, Listener> lst : listeners) {
			active().addListener(lst.getOne(), lst.getTwo());
		}

		active().setLocation(loc);
	}

	public void setVisible(boolean b) {
		active().setVisible(b);
	}

	public void setMenuBar(Menu menuBar) {
		wndShell.setMenuBar(menuBar);

	}

	public Point getLocation() {
		return active().getLocation();
	}



}
