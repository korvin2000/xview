package org.libjpegturbo.turbojpeg.test;

import java.awt.event.KeyAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Main
{
    Display display = new Display();
    Shell shell = new Shell(display);
    Button buttonOpen = new Button(shell, SWT.NONE);
    Button buttonClose = new Button(shell, SWT.NONE);
    Shell shellFS;

    public static void main(String[] args)
    {
        new Main();
    }

    Main()
    {
        shell.setBounds(150, 50, 800, 400);

        buttonOpen.setText("Open");
        buttonOpen.setLocation(40, 150);
        buttonOpen.pack();
        buttonOpen.addSelectionListener(openListener);

        buttonClose.setText("Close");
        buttonClose.setLocation(240, 150);
        buttonClose.pack();
        buttonClose.addSelectionListener(closeListener);

        shell.addKeyListener(closeFSWithQ);
        shell.open();

        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

    SelectionListener openListener = new SelectionListener()
    {
        public void widgetSelected(SelectionEvent event)
        {
            open();
        }

        public void widgetDefaultSelected(SelectionEvent event)
        {

        }
    };

    SelectionListener closeListener = new SelectionListener()
    {
        public void widgetSelected(SelectionEvent event)
        {
            shell.close();
        }

        public void widgetDefaultSelected(SelectionEvent event)
        {

        }
    };

    KeyListener closeFSWithQ = new KeyListener()
    {
        public void keyPressed(KeyEvent e)
        {
            if (e.keyCode == 113)
            {
                close();
            }
        }

		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}
    };

    void close()
    {
        shellFS.setFullScreen(false);
        shellFS.close();
    }

    void open()
    {
        shellFS = new Shell(Display.getDefault(), SWT.NO_TRIM | SWT.ON_TOP);//SWT.SHELL_TRIM
        shellFS.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
        shellFS.addKeyListener(closeFSWithQ);
        shellFS.setMaximized (true);
        shellFS.setFullScreen(true);
        shellFS.layout(true);
        shellFS.open();
    }


    /**
     * Converts a buffered image to SWT <code>ImageData</code>.
     *
     * @param bufferedImage  the buffered image (<code>null</code> not
     *         permitted).
     *
     * @return The image data.
     */
    public static ImageData convertToSWT(BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
    		DirectColorModel colorModel = (DirectColorModel)bufferedImage.getColorModel();
    		PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask());
    		ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);
    		for (int y = 0; y < data.height; y++) {
    			for (int x = 0; x < data.width; x++) {
    				int rgb = bufferedImage.getRGB(x, y);
    				int pixel = palette.getPixel(new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
    				data.setPixel(x, y, pixel);
    				if (colorModel.hasAlpha()) {
    					data.setAlpha(x, y, (rgb >> 24) & 0xFF);
    				}
    			}
    		}
    		return data;
    	}
        else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel)
                    bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
                        blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        } else  if (bufferedImage.getColorModel() instanceof ComponentColorModel) {
            ComponentColorModel colorModel = (ComponentColorModel)bufferedImage.getColorModel();

            //ASSUMES: 3 BYTE BGR IMAGE TYPE

            PaletteData palette = new PaletteData(0x0000FF, 0x00FF00,0xFF0000);
            ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette);

            //This is valid because we are using a 3-byte Data model with no transparent pixels
            data.transparentPixel = -1;

            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
                    data.setPixel(x, y, pixel);
                }
            }
            return data;
        }


       ///
        return null;
    }
//info
    /*
     * http://www.tentech.ca/index.php/2008/11/java2d-under-swt-coercing-graphics2d-functionality-into-swt/
     * http://www.eclipse.org/articles/article.php?file=Article-Swing-SWT-Integration/index.html
     */

}