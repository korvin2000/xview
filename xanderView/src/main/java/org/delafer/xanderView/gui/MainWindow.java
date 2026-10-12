package org.delafer.xanderView.gui;

import static org.eclipse.swt.SWT.*;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;

import net.j7.commons.strings.StringUtils;

import org.delafer.xanderView.common.ImageSize;
import org.delafer.xanderView.file.*;
import org.delafer.xanderView.file.CopyService.CopyObserver;
import org.delafer.xanderView.file.entry.ImageAbstract;
import org.delafer.xanderView.file.entry.ImageDec;
import org.delafer.xanderView.file.entry.ImageEnc;
import org.delafer.xanderView.general.State;
import org.delafer.xanderView.gui.config.ApplConfiguration;
import org.delafer.xanderView.gui.config.OrientationStore;
import org.delafer.xanderView.gui.helpers.*;
import org.delafer.xanderView.gui.splash.SplashText;
import org.delafer.xanderView.gui.splash.SplashWindow;
import org.delafer.xanderView.orientation.OrientationCommons.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public final class MainWindow extends ImageLoader{

	private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

	private MultiShell shell;
	private Display display;
	private Composite cmpEmbedded;
	private ImageCanvas panel;
	private static CommonContainerExt pointer;
	LazyUpdater updater = null;
	private static final Point SHELL_SIZE = new Point(940, 720);
	private ImageSize displaySize;

	public MainWindow() {
	}

	public void open(String path) {
		if (path != null && path.startsWith("\"") && path.endsWith("\"")) path = path.substring(1, path.length()-1);
		initPath(path);
		intitialize();
		show();
		runGlobalEventLoop();

	}

	public void openFile(String path) {
		initPath(path);
		if (pointer.getCurrent() != null ){
			loadImageAsync(pointer, pointer.getCurrent(), panel);
		}
	}

	private void initPath(String path) {
		if (pointer != null) {
			pointer.close();
		}
		pointer = new CommonContainerExt(path);
	}

	private void loadImageAsync(CommonContainer container, ImageAbstract<?> entry, ImageCanvas targetPanel) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				loadImage(container, entry, targetPanel);
			}
		});
	}

	protected void show () {

		shell = new MultiShell(display,  0  );
		shell.setLayout(new FillLayout(HORIZONTAL));
		shell.setSize( SHELL_SIZE );

		shell.addListener(SWT.Move, new Listener() {

	        public void handleEvent(Event e) {
	            Rectangle rect = shell.getBounds();
	            ApplConfiguration cfg = ApplConfiguration.instance();
	            cfg.set(ApplConfiguration.CFG_POS_X, rect.x);
	            cfg.set(ApplConfiguration.CFG_POS_Y, rect.y);
	            cfg.set(ApplConfiguration.CFG_WIDTH, rect.width);
	            cfg.set(ApplConfiguration.CFG_HEIGHT, rect.height);
	        }

	    });

		shell.addListener(SWT.Resize, new Listener() {
	        public void handleEvent(Event e) {
	        	if(updater == null || !updater.isAlive()) {
	    			updater = new LazyUpdater(MainWindow.this.panel);
	    		}
	    		updater.update();

	    		e.doit = false;
	        }

	    });

		Monitor monitor = getActiveMonitor();
		locateWindow(monitor);

		createMenuBar();
		createToolBar();
		createImageCanvas();
		shell.open ();
		shell.fullscreenMandatory();
//		shell.uiUpdate(false);
		if (pointer.getCurrent() != null ){
			loadImageAsync(pointer, pointer.getCurrent(), panel);
		}


	}

	private void createImageCanvas() {
		panel = new ImageCanvas (shell);

		cmpEmbedded = new Composite(shell.active(),  EMBEDDED | NO_REDRAW_RESIZE  | NO_BACKGROUND  | NO_SCROLL | DOUBLE_BUFFERED );

		cmpEmbedded.setLayout(null);
		java.awt.Frame awtFrame = SWT_AWT.new_Frame( cmpEmbedded );
		awtFrame.setResizable(false);
		awtFrame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if(e.getClickCount()==2){
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							toggleFullscreen();
						}
					});

		        }
			}

		});
		awtFrame.add(panel);
	}

	private void locateWindow(Monitor monitor) {

		ApplConfiguration cfg = ApplConfiguration.instance();
		int x = cfg.getInt(ApplConfiguration.CFG_POS_X);
		int y = cfg.getInt(ApplConfiguration.CFG_POS_Y);
		if (x == 0 && y == 0) {
			Rectangle bounds = monitor.getBounds ();
			Rectangle rect = shell.getBounds ();
			x = bounds.x + (bounds.width - rect.width) / 2;
			y = bounds.y + (bounds.height - rect.height) / 2;
		}
		shell.setLocation (x, y);
	}

	private void intitialize() {
		display = new Display ();
		Rectangle rect = display.getPrimaryMonitor().getClientArea();
		displaySize = new ImageSize(rect.width, rect.height);
		display.setWarnings(false);
		display.addFilter(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) { bindKeyEvent(e); }
		});
	}

	private void terminateApp() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				UIHelpers.sleep(250);
				shell.close();
			}
		});
	}

	protected void bindKeyEvent(Event e) {
		switch (e.keyCode) {
		case SWT.ESC:
			terminateApp();
			break;
		case SWT.ARROW_DOWN:
			loadImage(pointer, pointer.getPrevious10(), panel);
			break;
		case SWT.ARROW_LEFT:
		case SWT.BS:
		case SWT.PAGE_UP:
			loadImage(pointer, pointer.getPrevious(), panel);
			break;
		case '2':
			changeGamma(true);
			break;
		case '1':
			changeGamma(false);
			break;
		case SWT.KEYPAD_8:
			panel.moveVr(true);;
			panel.showImage();
			break;
		case SWT.KEYPAD_2:
			panel.moveVr(false);
			panel.showImage();
			break;
		case SWT.KEYPAD_6:
			panel.moveHr(true);;
			panel.showImage();
			break;
		case SWT.KEYPAD_4:
			panel.moveHr(false);;
			panel.showImage();
			break;
		case 'p':
			//random access
			pointer.switchRandomAccess();
			new SplashWindow(shell.active(), pointer.isRandomMode() ? State.Special1 : State.Success);
			break;
		case 'q':
			//only non existing: "Q"
			pointer.switchOnlyExisting();
			new SplashWindow(shell.active(), pointer.isOnlyExisting() ? State.Special1 : State.Success);
			break;
		case 's':
			//S -> save
			ImageAbstract<?> current = pointer.getCurrent();
			if (current != null) {
				OrientationStore.instance().setOrientation(current.CRC(), panel.getOrientation());
				OrientationStore.instance().setScaleConst(current.CRC(), panel.getScaleFactor());
				OrientationStore.instance().setHrOffset(current.CRC(), panel.offsetX);
				OrientationStore.instance().setVrOffset(current.CRC(), panel.offsetY);
				OrientationStore.instance().setGammaConst(current.CRC(), panel.gammaIdx);
			}
			new SplashWindow(shell.active(), State.Special1);
			break;
		case SWT.KEYPAD_MULTIPLY:
			panel.scaleReset();
			panel.showImage();
			break;
		case SWT.KEYPAD_ADD:
			panel.scaleUp();
			panel.showImage();
			break;
		case SWT.KEYPAD_SUBTRACT:
			panel.scaleDown();
			panel.showImage();
			break;
		case SWT.F1:
			//F1 F1 F1
			//copy encrypted
			//enc -> enc
			//dec -> enc
			//copy always encrypted
			CopyService.instance().copy(ImageEnc.getEncrypted(pointer.getCurrent()), new CopyObserver(shell, panel));
			break;
		case SWT.F8:
		case SWT.F7:
		case SWT.F9:
			//copy decrypted
			//enc -> dec
			//dec -> dec
			CopyService.instance().copy(pointer.getCurrent(), new CopyObserver(shell, panel));
			break;
		case SWT.F3:
			//copy "as is" unchanged
			//enc -> enc
			//dec -> dec
			CopyService.instance().copy(ImageDec.getOriginal(pointer.getCurrent()), new CopyObserver(shell, panel));
//			SplashWindow splash = new SplashWindow(shell.active(), res);
			break;
		case 'k':
		case 'l':
		case ';':
			panel.rotate(Action.RotateLeft);
			panel.showImage();
			break;
		case 'r':
			panel.rotate(Action.RotateRight);
			panel.showImage();
			break;
		case 'v':
			panel.rotate(Action.FlipVertical);
			panel.showImage();
			break;
		case 'h':
			panel.rotate(Action.FlipHorizontal);
			panel.showImage();
			break;
		case SWT.ARROW_UP:
			loadImage(pointer, pointer.getNext10(), panel);
			break;
		case SWT.ARROW_RIGHT:
		case SWT.PAGE_DOWN:
			loadImage(pointer, pointer.getNext(), panel);
			break;
		case SWT.CR:
			toggleFullscreen();
			break;
		case SWT.SPACE:
			changeCopyDir(0);
			break;
		case 'm':
			changeCopyDir(1);

			break;
		case 'n':
			changeCopyDir(-1);
			break;
		default:
			log.debug("unbound key code: {}", e.keyCode);
			break;
		}
	}


	private void changeCopyDir(int forward) {
		if (forward != 0) {
			ApplConfiguration cfg = ApplConfiguration.instance();
			if (forward > 0) cfg.nextCopyDir(); else cfg.prevCopyDir();
		}
		new SplashText(shell.active(), ApplConfiguration.instance().getCopyDir());
		MainWindow.this.shell.updateInfo();
		if (forward != 0) CopyService.checkAsync(new CopyService.Observer() {
			public void done(CopyService instance) {
				boolean exist = instance.imageExist(pointer.getCurrent());

				if (exist && !panel.text.endsWith("*")) panel.text = panel.text + "*";
				if (!exist && panel.text.endsWith("*")) panel.text = panel.text.substring(0, panel.text.length()-1);

				panel.showImage();
			}
		});
	}

	private void changeGamma(boolean direct) {
		panel.changeGamma(direct);
		LazyUpdaterAbstract.doTask(new Runnable() {
			public void run() {
				panel.preRenderImage();
				panel.showImage();
			}
		});
	}

	private void toggleFullscreen() {
		boolean isFullScreen  = !shell.getFullScreen();

		shell.setFullScreen(isFullScreen);

		cmpEmbedded.setParent(shell.active());

		shell.uiUpdate(false);

		panel.preRenderImage();
		panel.showImage();
//
		shell.sendResized();

	}



	Menu createMenuBar() {
		// Menu bar.
		Menu menuBar = new Menu(shell.wndShell(), SWT.BAR);
		shell.setMenuBar(menuBar);
		createFileMenu(menuBar);
		return menuBar;
	}

	void createFileMenu(Menu bar) {
		MenuItem fileItem = new MenuItem (bar, SWT.CASCADE);
		fileItem.setText ("&Menu");
		Menu submenu = new Menu (shell.wndShell(), SWT.DROP_DOWN);
		fileItem.setMenu (submenu);
		UIHelpers.addMenuItem(submenu, "Select image to &Open\tCtrl+O", SWT.MOD1 + 'O', new Listener() {
			public void handleEvent(Event event) {
				 FileDialog dialog = new FileDialog(MainWindow.this.shell.wndShell(), SWT.OPEN);
				   dialog.setFilterExtensions(new String [] {"*.jpg;*.jpeg;*.jpe;*.jfif;*.jif;*.jfi;*.bmp;*.rle;*.dib;*.png,*.gif;*.;*.zip;*.jzip;*.cry"});
				   dialog.setFilterPath(pointer.getLocation());
				   String result = dialog.open();
				   if (StringUtils.isEmpty(result)) return ;
				   MainWindow.this.openFile(result);
			}
		} );

		UIHelpers.addMenuItem(submenu, "Select target copy &Directory\tCtrl+D", SWT.MOD1 + 'D', new Listener() {
			public void handleEvent(Event event) {
				 DirectoryDialog dialog = new DirectoryDialog(MainWindow.this.shell.wndShell(), SWT.OPEN);
				 dialog.setFilterPath(ApplConfiguration.instance().getCopyDir());
   			     String result = dialog.open();
   			     if (StringUtils.isEmpty(result)) return ;
   			     ApplConfiguration.instance().setCopyDir(result);
   			     MainWindow.this.shell.updateInfo();
   			     CopyService.instance().init();
			}
		} );

		UIHelpers.addMenuItem(submenu, "Exit", 0, new Listener() {
			@Override
			public void handleEvent(Event event) {
				shell.close();
				System.exit(0);
			}
		});

	}

	private void createToolBar() {
		//ImageRepository.loadImages(display);
	}


	protected Monitor getActiveMonitor() {

		return getClosestMonitor(Display.getCurrent(), shell.getLocation());
	}

	private static Monitor getClosestMonitor(Display toSearch, Point toFind) {
		int closest = Integer.MAX_VALUE;

		Monitor[] monitors = toSearch.getMonitors();
		Monitor result = monitors[0];

		for (int idx = 0; idx < monitors.length; idx++) {
			Monitor current = monitors[idx];

			Rectangle clientArea = current.getClientArea();

			if (clientArea.contains(toFind)) {
				return current;
			}

			int distance = Geometry.distanceSquared(Geometry.centerPoint(clientArea), toFind);
			if (distance < closest) {
				closest = distance;
				result = current;
			}
		}

		return result;
	}



	private void runGlobalEventLoop()
	{
		Shell[] shells;
		while (!display.isDisposed ())
			if (!display.readAndDispatch()) {
				shells = display.getShells();
				if (shells.length < 2) break;
				display.sleep();
			}

		if (pointer != null) {
			pointer.close();
		}
		display.dispose ();
	}

	public Dimension getSize() {
		return panel.getSize();
	}

	public ImageSize displaySize() {
		return displaySize;
	}



	}
