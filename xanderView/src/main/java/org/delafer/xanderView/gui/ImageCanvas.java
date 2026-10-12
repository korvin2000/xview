package org.delafer.xanderView.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.AttributedString;

import net.j7.commons.base.CommonUtils;
import net.j7.commons.strings.StringUtils;

import org.delafer.xanderView.common.FifoMap;
import org.delafer.xanderView.common.ImageSize;
import org.delafer.xanderView.gui.config.OrientationStore.ImageData;
import org.delafer.xanderView.gui.helpers.MultiShell;
import org.delafer.xanderView.orientation.*;
import org.delafer.xanderView.orientation.OrientationCommons.Action;
import org.delafer.xanderView.orientation.OrientationCommons.Orientation;
import org.delafer.xanderView.process.GammaCorrection;
import org.delafer.xanderView.scale.ScaleFactory;

import com.carrotsearch.hppc.IntFloatHashMap;

public class ImageCanvas extends Canvas implements MouseListener  {
	public static final Color l_red       = new Color(255, 16, 52);
	private static final float scaleFactor = 1.05f;
	private static final float gammaFactor = 1.05f;
	private static final long serialVersionUID = 9162619010168531038L;
	BufferedImage imageSource;
	BufferedImage drawImage;
	String text;

	Orientation orientation;
	Orientation orientationDefault;
	int scaleIdx;
	int offsetX;
	int offsetY;
	int gammaIdx;

	static IntFloatHashMap scaleData;
	static IntFloatHashMap gammaData;
	static {
		scaleData = new IntFloatHashMap(63);
		float s1 = 1f, s2 = s1;
		for (int i = 1; i < 32; i++) {
			s1 /= ImageCanvas.scaleFactor;
			s2 *= ImageCanvas.scaleFactor;
			scaleData.put(-i, s1);
			scaleData.put(i, s2);
		}

		gammaData = new IntFloatHashMap(63);
		s1 = 1f; s2 = s1;
		for (int i = 1; i < 32; i++) {
			s1 /= ImageCanvas.gammaFactor;
			s2 *= ImageCanvas.gammaFactor;
			gammaData.put(-i, s1);
			gammaData.put(i, s2);
		}
	}

//	LazyUpdater updater;
	MultiShell shell;

	private static FifoMap<Integer, Font> fonts = new FifoMap<Integer, Font>(4) {
		public void dispose(Integer key, Font value) {}
	};
	static int lastFontSize = 20;

	private synchronized final static Font getFont(int size) {
		Font font = fonts.get(size);
		lastFontSize = size;
		return fonts.computeIfAbsent(size, k -> new Font("MS Reference Sans Serif", Font.BOLD, size));
	}



    public ImageCanvas(MultiShell shell) {
    	super();
    	this.shell = shell;
        this.setBackground(Color.BLACK);
        this.setForeground(Color.BLACK);
        //this.setDoubleBuffered(true);
        this.setIgnoreRepaint(false);

        addMouseListener(this);
        //this.setOpaque(true);
    }

    public void setExist(boolean exists) {
    	if (exists && !text.endsWith("*")) text += "*";
    	if (!exists && text.endsWith("*")) text = text.substring(0, text.length()-1);
    }

    public boolean setImage(BufferedImage image, String text, ImageData imgData) {
   	 	this.imageSource = image;
   	 	this.text = text;
   	 	this.orientationDefault = getDirectionDefault();
   	 	this.orientation = CommonUtils.nvl(imgData.getOrientation(), orientationDefault);
   	 	this.scaleIdx = imgData.getScaleConst();
   	 	this.offsetX = imgData.getHrOffset();
   	 	this.offsetY = imgData.getVrOffset();
   	 	this.gammaIdx = imgData.getGamma();
   	 	return preRenderImage();
    }

    private Orientation getDirectionDefault() {
    	ImageSize imgSize = getImageSize(false);
    	ImageSize cnvSize = getCanvasImageSize();

    	boolean rotate = (imgSize.width() > imgSize.height()) != (cnvSize.width() > cnvSize.height());
    	rotate = rotate && (max(imgSize.width(), imgSize.height()) / min(imgSize.width(), imgSize.height()) >= ImageCanvas.scaleFactor);

    	return !rotate ? Orientation.Original : Orientation.RotatedLeft;
	}



	private float max(int arg1, int arg2) {
		return (float)(arg1 > arg2 ? arg1 : arg2);
	}

	private float min(int arg1, int arg2) {
		return (float)(arg1 < arg2 ? arg1 : arg2);
	}


	public void scaleReset() {
		scaleIdx = 0;
		offsetX = 0;
		offsetY = 0;
		gammaIdx = 0;
		preRenderImage();
	}

	public void moveHr(boolean leftToRight) {
		if (leftToRight && offsetX < 31) offsetX++;
		if (!leftToRight && offsetX > -31) offsetX--;
	}

	public void moveVr(boolean upstairs) {
		if (!upstairs && offsetY < 31) offsetY++;
		if (upstairs && offsetY > -31) offsetY--;
	}

	public void changeGamma(boolean increase) {
		if (increase && gammaIdx < 31) gammaIdx++;
		if (!increase && gammaIdx > -31) gammaIdx--;
//		preRenderImage();
	}

	public void scaleUp() {
		if (scaleIdx<31) scaleIdx++;

		preRenderImage();
	}

	public void scaleDown() {
		if (scaleIdx>-31) scaleIdx--;

		preRenderImage();
	}


	public Orientation rotate(Action action) {
    	orientation = orientation.newState(action);
    	preRenderImage();
    	return orientation;
    }

    private ImageSize getCanvasImageSize() {
    	return new ImageSize(getWidth(), getHeight());
    }

    private ImageSize getImageSize(boolean swapXY) {
    	ImageSize is = new ImageSize(imageSource.getWidth(null), imageSource.getHeight(null));
    	if (swapXY) is.swapXY();
    	return is;
    }


    public boolean preRenderImage() {
    	if (null == imageSource || orientation == null) return false;
    	boolean swapXY = Orientation.Original != orientation && orientation.swapXY();
    	ImageSize imgSize = getImageSize(swapXY);
    	ImageSize cnvSize = getCanvasImageSize();

    	if (!imgSize.equals(cnvSize)) {
    		ImageSize size = OrientationCommons.getNewSize(imgSize.width(), imgSize.height(), cnvSize.width(), cnvSize.height());
    		if (size.empty()) return false;

    		if (scaleIdx != 0) size.scale(scaleData.get(scaleIdx));
    		drawImage = ScaleFactory.instance(imgSize).resize(imageSource, !swapXY ? size.width() : size.height(), !swapXY ? size.height() : size.width());
    	} else {
    		drawImage = imageSource;
    	}

    	if (!Orientation.Original.equals(orientation)) {
    		CommonRotator ir = new Rotator2D();
        	drawImage = ir.rotate(drawImage, orientation);
    	}

    	if (0 != gammaIdx) {
    		float gamma = gammaData.get(gammaIdx);
    		drawImage = GammaCorrection.gammaCorrection(drawImage, gamma);
    	}


//    	new Thread() {
//			public void run() {
////		    	GammaFilter filter = new GammaFilter(0.5f);
//				SwimFilter filter = new SwimFilter();
//				filter.setTurbulence(0.4f);
//				filter.setAmount(50f);
//				filter.setStretch(10);
//				filter.setTime(1f);
//				drawImage = filter.filter(drawImage, null);
//				ImageCanvas.this.showImage();
//			}
//
//    	}.start();

    	return true;
    }


    public void showImage() {
    	if (EventQueue.isDispatchThread()) {
    		repaint();
    	} else {
    		EventQueue.invokeLater(new Runnable() {
    			@Override
    			public void run() {
    				repaint();
    			}
    		});
    	}
    }


	public void paintComponent(Graphics g) {
        //super.paintComponent(g);  // Paint background
        paintCanvas(this, g);

    }

	public void paintCanvas(ImageCanvas panel, Graphics g) {
//		g.setColor(Color.BLACK);
//		g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);
        if (panel.drawImage!=null) {
        	Rectangle dim = this.getBounds();
        	int x = (dim.width - panel.drawImage.getWidth(null)) >> 1;
        	int y  = (dim.height - panel.drawImage.getHeight(null)) >> 1;

        	g.drawImage(panel.drawImage, x + (offsetX << 4), y + (offsetY << 4), null);
        }

        if (!StringUtils.isEmpty(panel.text)) {
        	AttributedString as = new AttributedString(panel.text);
	        fontSize(this.getBounds().width);
			Font font = getFont(fontSize(this.getBounds().width));
            as.addAttribute(TextAttribute.FONT, font);
            as.addAttribute(TextAttribute.FOREGROUND,  (panel.text.indexOf('*')<0 ? Color.GREEN : l_red));
            g.setFont(font);

            drawText(g, as);
        }
	}

	private int fontSize(int width) {
		int part2 =  (int) ( Math.round(Math.pow(width, 1.0f / 1.3f)) / 15f);
		int size = 10 + part2;
		//System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+size);
		return size;
	}

	private void drawText(Graphics g, AttributedString as) {
		int y =  this.getBounds().height - (lastFontSize/2);
		int x =  lastFontSize /*+ ((shell.getFullScreen()) ? 10 : 0)*/;

		if (shell.getFullScreen()) {
		AffineTransform at = new AffineTransform();
		at.setToQuadrantRotation(3, x,y);
		((Graphics2D)g).setTransform(at);
		}

		g.drawString(as.getIterator(), x, y);
	}

	public Orientation getOrientation() {
		return orientation != orientationDefault ? orientation : null;
	}

	public int getScaleFactor() {
		return this.scaleIdx;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		paintCanvas(this, g);
	}

	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}


}
