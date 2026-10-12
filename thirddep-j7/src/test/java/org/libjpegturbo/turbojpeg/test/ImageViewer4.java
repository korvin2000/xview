package org.libjpegturbo.turbojpeg.test;


import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * An image viewer using swt.
 *
 * <li>A GC can be created on an Image and then use it to draw into the Image.</li>
 * <li>To display very large image, pixel data is loaded into ImageData and then
 * tiles of the data are used to create Image's for display.</li>
 *
 * This is an experimental version modified from ImageViewer3.  To reduce time lag between
 * clear and update during scroll, a large Image is drawn to the display, so that it would use
 * scroll in the offscreen image instead of wait for update from the PaintEvent.  The PaintEvent
 * then fill up the offscreen section that are scrolled away.
 *
 * Update is much faster. There are no visiable blank region for large scroll or jump scroll.
 * But the update of the larger offscreen Image make scrolling more jumpy even for small scroll.
 * So it may be better off simply use the ImageViewer3 approach if really needed to handle
 * very large image.
 *
 * @bugs A mess, nothing work except a little bit of horizontal scrolling.
 * @author chrisl
 */
public class ImageViewer4 {

	////////////////////////////////////////////////////////////////////////

	private static final String NAME = "ImageViewer4x";

	private static final String[] IMAGE_SUFFICES =
		new String[] {
			"*.bmp; *.gif; *.ico; *.jpg; *.pcx; *.png; *.tif",
			"*.bmp",
			"*.gif",
			"*.ico",
			"*.jpg",
			"*.pcx",
			"*.png",
			"*.tif" };

	private static final String[] IMAGE_FILTER_NAMES =
		new String[] {
			i18n("All_images") + " (bmp, gif, ico, jpg, pcx, png, tif)",
			"BMP (*.bmp)",
			"GIF (*.gif)",
			"ICO (*.ico)",
			"JPEG (*.jpg)",
			"PCX (*.pcx)",
			"PNG (*.png)",
			"TIFF (*.tif)" };

	////////////////////////////////////////////////////////////////////////

	private Display fDisplay;
	private Shell fShell;
	private Font fFont;
	private Color fWhiteColor, fBlackColor, fLightColor;
	private Cursor fCrossCursor;
	private Image fImage;
	private ImageData[] fImageDatas;
	private ImageData fImageData; // the currently-displayed image data
	private int fImageDataIndex;
	private double fScale;
	private int fX, fY; // Top left corner of area being displayed.
	private int fImageX, fImageY, fImageW, fImageH; // Top left corner of fImage.
	private int fClientW, fClientH;
	private String fLastPath;
	private boolean fTransparent, fShowMask;
	private boolean fIncremental;
	//
	private long fLoadTime;
	private String fFilename;
	//
	// Widgets
	private Combo fScaleCombo;
	private Canvas fImageCanvas;
	private Sash fSash;
	private Label fDataLabel, fStatusLabel;
	private StyledText fDataText;
	private Label fInfoLabel;

	////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {

		Display display = new Display();
		ImageViewer4 app = new ImageViewer4();
		Shell shell = app.open(display);

		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
		display.dispose();
	}

	////////////////////////////////////////////////////////////////////////

	public Shell open(Display display) {

		// Create a window and set its title.
		this.fDisplay = display;
		this.fShell = new Shell(fDisplay);

		// Create colors and fonts.
		fWhiteColor = new Color(display, 255, 255, 255);
		fBlackColor = new Color(display, 0, 0, 0);
		fLightColor = new Color(display, 0xd8, 0xd8, 0xd8);
		fFont = new Font(display, "verdana", 11, 0);
		fCrossCursor = new Cursor(display, SWT.CURSOR_CROSS);

		fShell.setText(NAME);

		// Hook resize and dispose listeners.
		fShell.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				resizeShell(event);
			}
		});
		fShell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				e.doit = true;
			}
		});
		fShell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});

		// Add a menu bar and widgets.
		createMenuBar();
		createWidgets();
		fShell.pack();

		// Create a GC for drawing, and hook the listener to dispose it.
		//		fImageCanvasGC = new GC(fImageCanvas);
		//		fImageCanvas.addDisposeListener(new DisposeListener() {
		//			public void widgetDisposed(DisposeEvent e) {
		//				fImageCanvasGC.dispose();
		//			}
		//		});

		// Open the window
		fShell.open();
		return fShell;
	}

	void dispose() {
		// Clean up.
		fWhiteColor.dispose();
		fBlackColor.dispose();
		fLightColor.dispose();
		fFont.dispose();
		fCrossCursor.dispose();
	}

	Shell getShell() {
		return fShell;
	}

	Display getDisplay() {
		return fDisplay;
	}

	void createWidgets() {
		// Add the widgets to the shell in a grid layout.
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.numColumns = 2;
		fShell.setLayout(layout);

		// Add a composite to contain some control widgets across the top.
		Composite controls = new Composite(fShell, SWT.NULL);
		controls.setLayoutData(UIUtil.fillCell(1, 1, false, false));

		// RowLayout is top aligned. To make label text at middle, need to set label height.
		fInfoLabel = new Label(fShell, SWT.NULL);
		fInfoLabel.setToolTipText("Image info");
		fInfoLabel.setText("Image info");
		fInfoLabel.setLayoutData(UIUtil.fillCell(1, 1, true, false));
		// Combo to change the x scale.
		String[] values =
			{
				"5 %",
				"10 %",
				"25 %",
				"33 %",
				"50 %",
				"75 %",
				"100 %",
				"125 %",
				"133 %",
				"150 %",
				"175 %",
				"200 %",
				"300 %",
				"500 %",
				"750 %",
				"1000 %",
				};
		controls.setLayout(null);
		fScaleCombo = new Combo(controls, SWT.DROP_DOWN);
		fScaleCombo.setBounds(3, 2, 93, 21);
		for (int i = 0; i < values.length; i++) {
			fScaleCombo.add(values[i]);
		}
		fScaleCombo.setToolTipText("Scale");
		fScaleCombo.select(fScaleCombo.indexOf("100 %"));
		fScaleCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scaleImage();
			}
		});

		// Canvas to show the image.
		fImageCanvas = new Canvas(fShell, SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_REDRAW_RESIZE);
		fImageCanvas.setBackground(fWhiteColor);
		fImageCanvas.setCursor(fCrossCursor);
		fImageCanvas.setLayoutData(UIUtil.fillCell(15, 2, 1024, 768, true, true));
		fImageCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				//if (fImage != null)
				paintImage(event);
			}
		});
		fImageCanvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent event) {
				showColorAt(event.x, event.y);
			}
		});

		// Set up the image canvas scroll bars.
		ScrollBar horizontal = fImageCanvas.getHorizontalBar();
		horizontal.setVisible(true);
		horizontal.setMinimum(0);
		horizontal.setEnabled(false);
		horizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollHorizontally((ScrollBar) event.widget);
			}
		});
		ScrollBar vertical = fImageCanvas.getVerticalBar();
		vertical.setVisible(true);
		vertical.setMinimum(0);
		vertical.setEnabled(false);
		vertical.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				scrollVertically((ScrollBar) event.widget);
			}
		});

		// Label to show status and cursor location in image.
		fStatusLabel = new Label(fShell, SWT.NULL);
		fStatusLabel.setText("");
		fStatusLabel.setLayoutData(UIUtil.fillCell(1, 2, true, false));
	}

	Menu createMenuBar() {
		// Menu bar.
		Menu menuBar = new Menu(fShell, SWT.BAR);
		fShell.setMenuBar(menuBar);
		createFileMenu(menuBar);
		return menuBar;
	}

	void createFileMenu(Menu menuBar) {
		// File menu
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("File");
		Menu fileMenu = new Menu(fShell, SWT.DROP_DOWN);
		item.setMenu(fileMenu);

		item = new MenuItem(fileMenu, SWT.NULL);
		item.setText("&Open");
		item.setAccelerator(SWT.CTRL + 'O');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				openFile();
			}
		});

		item = new MenuItem(fileMenu, SWT.NULL);
		item.setText("Open &URL");
		item.setAccelerator(SWT.CTRL + 'U');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				openURL();
			}
		});

		new MenuItem(fileMenu, SWT.SEPARATOR);

		// File -> Exit
		item = new MenuItem(fileMenu, SWT.NULL);
		item.setText("Exit");
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				getShell().close();
			}
		});

		// Test menu
		item = new MenuItem(menuBar, SWT.CASCADE);

	}

	////////////////////////////////////////////////////////////////////////

	void openFile() {
		resetScaleCombo();

		// Get the user to choose an image file.
		FileDialog fileChooser = new FileDialog(fShell, SWT.OPEN);
		if (fLastPath != null)
			fileChooser.setFilterPath(fLastPath);
		fileChooser.setFilterExtensions(IMAGE_SUFFICES);
		fileChooser.setFilterNames(IMAGE_FILTER_NAMES);
		String filename = fileChooser.open();
		fLastPath = fileChooser.getFilterPath();
		if (filename == null)
			return;

		Cursor waitCursor = new Cursor(fDisplay, SWT.CURSOR_WAIT);
		fShell.setCursor(waitCursor);
		fImageCanvas.setCursor(waitCursor);
		try {
			reset();
			ImageLoader loader = new ImageLoader();
			// Read the new image(s) from the chosen file.
			long startTime = System.currentTimeMillis();
			fImageDatas = loader.load(filename);
			fLoadTime = System.currentTimeMillis() - startTime;
			if (fImageDatas.length > 0) {
				// Cache the filename.
				fFilename = filename;
				// Display the first image in the file.
				fImageDataIndex = 0;
				displayImage(fImageDatas[fImageDataIndex]);
			}
		} catch (SWTException e) {
			showErrorDialog(i18n("Loading_lc"), filename, e);
		} finally {
			fShell.setCursor(null);
			fImageCanvas.setCursor(fCrossCursor);
			waitCursor.dispose();
		}
	}

	void openURL() {
		resetScaleCombo();

		// Get the user to choose an image URL.
		TextPrompter textPrompter = new TextPrompter(fShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		textPrompter.setText(i18n("OpenURLDialog"));
		textPrompter.setMessage(i18n("EnterURL"));
		String urlname = textPrompter.open();
		if (urlname == null)
			return;

		Cursor waitCursor = new Cursor(fDisplay, SWT.CURSOR_WAIT);
		fShell.setCursor(waitCursor);
		fImageCanvas.setCursor(waitCursor);
		try {
			reset();
			URL url = new URL(urlname);
			InputStream stream = url.openStream();
			ImageLoader loader = new ImageLoader();
			// Read the new image(s) from the chosen file.
			long startTime = System.currentTimeMillis();
			fImageDatas = loader.load(stream);
			fLoadTime = System.currentTimeMillis() - startTime;
			if (fImageDatas.length > 0) {
				fFilename = null;
				// Display the first image in the file.
				fImageDataIndex = 0;
				displayImage(fImageDatas[fImageDataIndex]);
			}
		} catch (Exception e) {
			showErrorDialog(i18n("Loading_lc"), urlname, e);
		} finally {
			fShell.setCursor(null);
			fImageCanvas.setCursor(fCrossCursor);
			waitCursor.dispose();
		}
	}

	void displayImage(ImageData newImageData) {
		// Dispose of the old image, if there was one.
		if (fImage != null)
			fImage.dispose();

		//		try {
		//			// Cache the new image and imageData.
		fImageData = newImageData;
		//			Rectangle bounds = fImageCanvas.getClientArea();
		//			fImage = getImage(0, 0, bounds.width, bounds.height);
		//		} catch (SWTException e) {
		//			showErrorDialog(i18n("Creating_from") + " ", fFilename, e);
		//			fImage = null;
		//			return;
		//		}

		// Update the widgets with the new image info.
		String string = createMsg(i18n("Analyzer_on"), fFilename);
		fShell.setText(string);

		fInfoLabel.setText(
			fFilename
				+ ": w="
				+ fImageData.width
				+ ", h="
				+ fImageData.height
				+ ", depth="
				+ fImageData.depth
				);
		fImageCanvas.redraw();
		resetScrollBars();
	}

	void paintImage(PaintEvent event) {
		if (false)
			System.out.println(
				NAME
					+ ".paintImage(): event="
					+ event.x
					+ ","
					+ event.y
					+ ","
					+ event.width
					+ ","
					+ event.height
					+ ", fX,fY="
					+ fX
					+ ","
					+ fY);
		if(fImageData==null) return;
		int x = event.x;
		int y = event.y;
		int width = event.width;
		int height = event.height;
		// Top level corner of area currently being displayed.
		int fx = fX;
		int fy = -fY;
		int w = Math.min(width, fImageData.width - fx - x);
		int h = Math.min(height, fImageData.height - fy - y);
		// Create a new image that enclose the damaged area.
		Rectangle clientarea = fImageCanvas.getClientArea();
		if (fImage == null
			|| fImage.isDisposed()
			|| clientarea.width != fClientW
			|| clientarea.height != fClientH) {
			w = Math.min(clientarea.width*3/2, fImageData.width - fx);
			h = Math.min(clientarea.height*3/2, fImageData.height - fy);
			if (true)
				System.err.println(NAME + ".getImage(): new: w=" + w + ", h=" + h);
			if (fImage != null && !fImage.isDisposed())
				fImage.dispose();
			fImage = createImage(fx, fy, w, h);
			fImageX = fx;
			fImageY = fy;
			fImageW = w;
			fImageH = h;
			fClientW = clientarea.width;
			fClientH = clientarea.height;
			event.gc.drawImage(fImage, 0, 0);
			return;
		}
		if (fImage != null) {
			// The damaged area.
			int ulx = fx + x + clientarea.width/2;
			int uly = fy + y;
			int lrx = ulx + w;
			int lry = uly + h;
			if (true)
				System.err.println(
					NAME
						+ ".getImage(): fx="
						+ fx
						+ ", fy="
						+ fy
						+ ", w="
						+ w
						+ ", h="
						+ h
						+ ",    damaged(x,y,w,h):"
						+ x
						+ ", "
						+ y
						+ ", "
						+ width
						+ ", "
						+ height
						+ ",    fImage(x,y,w,h): "
						+ fImageX
						+ ", "
						+ fImageY
						+ ", "
						+ fImageW
						+ ", "
						+ fImageH);
			//FIXME: check if damaged area outside fImageData.
			if (ulx > fImageData.width
				|| uly > fImageData.height
				|| ulx >= fImageX
				&& lrx <= fImageX + fImageW
				&& uly >= fImageY
				&& lry <= fImageY + fImageH) {
				// Damaged area is inside current buffered image.
				event.gc.drawImage(fImage, 0, 0);
				return;
			}
		}
		event.gc.drawImage(fImage, fImageX - fx, fImageY - fy);
		Image image = createImage(fx + x + clientarea.width/2, fy + y, w, h);
		//if (clientarea.width != fImageW || clientarea.height != fImageH) {
		// Size changed, create a new Image.
		//} else
		// Horizontal scroll
		if (true)
			System.err.println(NAME + ".getImage(): horizontal scroll: damaged x=" + (fx + x) + ", y=" + (fy + y));
		GC gc = new GC(fImage);
		gc.copyArea(0, 0, fImageW, fImageH, fImageX - fx, fImageY - fy);
		gc.drawImage(image, x + clientarea.width/2, y);
		gc.dispose();
		fImageX = fx;
		fImageY = fy;
		gc = new GC(fImageCanvas);
		gc.drawImage(image, x + clientarea.width/2, y);
		gc.dispose();
		image.dispose();
	}

	/**
	 * Extract an Image from the given area of the original image, fImageData.
	 */
	Image createImage(int x, int y, int w, int h) {
		if (true)
			System.err.println(NAME + ".createImage(): x=" + x + ", y=" + y + ", w=" + w + ", h=" + h);
		int[][] data = new int[h][w];
		for (int r = 0; r < h; ++r) {
			fImageData.getPixels(x, y + r, w, data[r], 0);
		}
		ImageData imagedata = new ImageData(w, h, fImageData.depth, fImageData.palette);
		for (int r = 0; r < h; ++r) {
			imagedata.setPixels(0, r, w, data[r], 0);
		}
		return new Image(fDisplay, imagedata);
	}

	/*
	 * Called when the mouse moves in the image canvas.
	 * Show the color of the image at the point under the mouse.
	 */
	void showColorAt(int mx, int my) {
		if(fImageData==null) return;
		int x = mx - fImageData.x + fX;
		int y = my - fImageData.y - fY;
		showColorForPixel(x, y);
	}

	/*
	 * Set the status label to show color information
	 * for the specified pixel in the image.
	 */
	void showColorForPixel(int x, int y) {
		if (x >= 0 && x < fImageData.width && y >= 0 && y < fImageData.height) {
			int pixel = fImageData.getPixel(x, y);
			RGB rgb = fImageData.palette.getRGB(pixel);

			Object[] args =
				{ new Integer(x), new Integer(y), new Integer(pixel), Integer.toHexString(pixel), rgb };
			if (pixel == fImageData.transparentPixel) {
				fStatusLabel.setText(createMsg("Color at transparent pixel: ", args));
			} else {
				//fStatusLabel.setText(Sprint.f("Color at: %d,%d=%06x").a(x).a(y).a(pixel).end());
			}
		} else {
			fStatusLabel.setText("");
		}
	}

	/*
	 * Called when a mouse down or key press is detected
	 * in the data text. Show the color of the pixel at
	 * the caret position in the data text.
	 */
	void showColorForData() {}

	////////////////////////////////////////////////////////////////////////

	/*
	 * Called when the image canvas' horizontal scrollbar is selected.
	 */
	void scrollHorizontally(ScrollBar scrollBar) {
		if (fImage == null)
			return;
		Rectangle canvasBounds = fImageCanvas.getClientArea();
		int width = (int) Math.round(fImageData.width);
		if (width > canvasBounds.width) {
			// Only scroll if the image is bigger than the canvas.
			int x = scrollBar.getSelection();
			if (width -x< canvasBounds.width) {
				// Don't scroll past the end of the image.
				x = width-canvasBounds.width;
			}
			fImageCanvas.scroll(-x, fY, -fX, fY, fImageW, fImageH, false);
			fX = x;
		}
	}

	/*
	 * Called when the image canvas' vertical scrollbar is selected.
	 */
	void scrollVertically(ScrollBar scrollBar) {
		if (fImage == null)
			return;
		Rectangle canvasBounds = fImageCanvas.getClientArea();
		int width = (int) Math.round(fImageData.width);
		int height = (int) Math.round(fImageData.height);
		if (height > canvasBounds.height) {
			// Only scroll if the image is bigger than the canvas.
			int y = -scrollBar.getSelection();
			if (y + height < canvasBounds.height) {
				// Don't scroll past the end of the image.
				y = canvasBounds.height - height;
			}
			fImageCanvas.scroll(fX, y, fX, fY, width, height, false);
			fY = y;
		}
	}

	/*
	 * Called when the ScaleX combo selection changes.
	 */
	void scaleImage() {
		try {
			String text = fScaleCombo.getText();
			int index = text.indexOf(' ');
			fScale = Float.parseFloat(text.substring(0, index)) / 100;
		} catch (NumberFormatException e) {
			fScale = 1;
			fScaleCombo.select(fScaleCombo.indexOf("100 %"));
		}
		if (fImage == null)
			return;
		// Scale image to a new buffer.
		fImageData = fImageDatas[fImageDataIndex];
		if (fScale != 1) {
			fImageData =
				fImageData.scaledTo(
					(int) Math.round(fImageData.width * fScale),
					(int) Math.round(fImageData.height * fScale));
		}
		resizeScrollBars();
		fX = fImageCanvas.getHorizontalBar().getSelection();
		fY = -fImageCanvas.getVerticalBar().getSelection();
		displayImage(fImageData);
	}

	// Reset the scale combos to 1.
	void resetScaleCombo() {
		fScale = 1;
		fScaleCombo.select(fScaleCombo.indexOf("100 %"));
	}

	// Reset the scroll bars to 0.
	void resetScrollBars() {
		if (fImageData == null)
			return;
		fX = 0;
		fY = 0;
		resizeScrollBars();
		fImageCanvas.getHorizontalBar().setSelection(0);
		fImageCanvas.getVerticalBar().setSelection(0);
	}

	void resizeScrollBars() {
		// Set the max and thumb for the image canvas scroll bars.
		ScrollBar horizontal = fImageCanvas.getHorizontalBar();
		ScrollBar vertical = fImageCanvas.getVerticalBar();
		Rectangle canvasBounds = fImageCanvas.getClientArea();
		int width = (int) Math.round(fImageData.width);
		if (width > canvasBounds.width) {
			// The image is wider than the canvas.
			// Set selection such as to keep top left corner of the image stationary.
			int x = horizontal.getSelection() * width / horizontal.getMaximum();
			horizontal.setEnabled(true);
			horizontal.setMaximum(width);
			horizontal.setSelection(x);
			horizontal.setThumb(canvasBounds.width);
			horizontal.setPageIncrement(canvasBounds.width / 2);
		} else {
			// The canvas is wider than the image.
			horizontal.setEnabled(false);
			if (fX != 0) {
				// Make sure the image is completely visible.
				fX = 0;
				fImageCanvas.redraw();
			}
		}
		int height = (int) Math.round(fImageData.height);
		if (height > canvasBounds.height) {
			// The image is taller than the canvas.
			// Set selection such as to keep top left corner of the image stationary.
			vertical.setEnabled(true);
			vertical.setMaximum(height);
			vertical.setThumb(canvasBounds.height);
			vertical.setPageIncrement(canvasBounds.height / 2);
		} else {
			// The canvas is taller than the image.
			vertical.setEnabled(false);
			if (fY != 0) {
				// Make sure the image is completely visible.
				fY = 0;
				fImageCanvas.redraw();
			}
		}
	}

	void resizeShell(ControlEvent event) {
		if (fImage == null || fShell.isDisposed())
			return;
		resizeScrollBars();
	}

	void reset() {
		fImageDatas = null;
		fImageData = null;
		if (fImage != null && !fImage.isDisposed())
			fImage.dispose();
		fImage = null;
	}

	////////////////////////////////////////////////////////////////////////

	/*
	 * Open an error dialog displaying the specified information.
	 */
	void showErrorDialog(String operation, String filename, Exception e) {
		MessageBox box = new MessageBox(fShell, SWT.ICON_ERROR);
		String message = createMsg(i18n("Error"), new String[] { operation, filename });
		String errorMessage = "";
		if (e != null) {
			if (e instanceof SWTException) {
				SWTException swte = (SWTException) e;
				errorMessage = swte.getMessage();
				if (swte.throwable != null) {
					errorMessage += ":\n" + swte.throwable.toString();
				}
			} else {
				errorMessage = e.toString();
			}
		}
		box.setMessage(message + errorMessage);
		box.open();
	}

	/*
	 * Return a String describing the specified image file type.
	 */
	static String fileTypeString(int filetype) {
		if (filetype == SWT.IMAGE_BMP)
			return "BMP";
		if (filetype == SWT.IMAGE_GIF)
			return "GIF";
		if (filetype == SWT.IMAGE_ICO)
			return "ICO";
		if (filetype == SWT.IMAGE_JPEG)
			return "JPEG";
		if (filetype == SWT.IMAGE_PNG)
			return "PNG";
		return i18n("Unknown_ac") + ": " + filetype;
	}

	static String createMsg(String msg, Object arg) {
		MessageFormat formatter = new MessageFormat(msg);
		return formatter.format(new Object[] { arg });
	}

	static String i18n(String str) {
		return str;
	}

	////////////////////////////////////////////////////////////////////////

	class TextPrompter extends Dialog {
		String message = "";
		String result = null;
		Shell dialog;
		Text text;
		public TextPrompter(Shell parent, int style) {
			super(parent, style);
		}
		public TextPrompter(Shell parent) {
			this(parent, SWT.APPLICATION_MODAL);
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String string) {
			message = string;
		}
		public String open() {
			dialog = new Shell(getParent(), getStyle());
			dialog.setText(getText());
			dialog.setLayout(new GridLayout());
			Label label = new Label(dialog, SWT.NULL);
			label.setText(message);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			text = new Text(dialog, SWT.SINGLE | SWT.BORDER);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.widthHint = 300;
			text.setLayoutData(data);
			Composite buttons = new Composite(dialog, SWT.NONE);
			GridLayout grid = new GridLayout();
			grid.numColumns = 2;
			buttons.setLayout(grid);
			buttons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			Button ok = new Button(buttons, SWT.PUSH);
			ok.setText(i18n("OK"));
			data = new GridData();
			data.widthHint = 75;
			ok.setLayoutData(data);
			ok.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					result = text.getText();
					dialog.dispose();
				}
			});
			Button cancel = new Button(buttons, SWT.PUSH);
			cancel.setText(i18n("Cancel"));
			data = new GridData();
			data.widthHint = 75;
			cancel.setLayoutData(data);
			cancel.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					dialog.dispose();
				}
			});
			dialog.setDefaultButton(ok);
			dialog.pack();
			dialog.open();
			while (!dialog.isDisposed()) {
				if (!getDisplay().readAndDispatch())
					getDisplay().sleep();
			}
			return result;
		}
	}

	////////////////////////////////////////////////////////////////////////

}
