package org.libjpegturbo.turbojpeg.test;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SWTDraw {

	
//	public void drawImage2(final BufferedImage img) {
//		if (null == img) System.out.println("Image is null");
//		long t1 = System.currentTimeMillis();
//		Display display = new Display();
//		
//		
//		JFrame  frame = new JFrame (); // java.awt.Frame
//		frame.setSize(600, 600);
//		frame.setVisible(true);
//		
//		
//		Canvas canvas = new Canvas() {
//	    private static final long serialVersionUID = 1;
//			@Override
//			public void paint(Graphics g) {
//				super.paint(g);
//				
//				// Draw image at its natural size first.
//		        g.drawImage(img, 0, 0, null); //85x62 image
//			}
//			
//		};
//		 
//		 frame.add(canvas);
//		
//		
//		Shell shellFS = SWT_AWT.new_Shell(display, canvas);
//		shellFS.setFullScreen(true);
//		shellFS.setRedraw(false);
//        shellFS.setMaximized (true);
//        
//        shellFS.open();
//        
//        while (!shellFS.isDisposed())
//        {
//            if (!display.readAndDispatch()) {
//                display.sleep();
//            }
//        }
//	}
	
	public void drawImage(BufferedImage img) {
		Display display = new Display();
		Shell shellFS = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND | SWT.APPLICATION_MODAL);
		shellFS.setLayout(new FillLayout(SWT.HORIZONTAL));
        shellFS.setMaximized (true);
        shellFS.setFullScreen(true);
        
        

		Composite cmpEmbedded = new Composite(shellFS, SWT.EMBEDDED  | SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND);
		java.awt.Frame frm = SWT_AWT.new_Frame( cmpEmbedded );
		cmpEmbedded.setLayout(null);

        ImagePanel  panel = new ImagePanel (img );
    	frm.add(panel);
    	panel.setBackground(Color.BLACK);
    	
    	shellFS.open();

        while (!shellFS.isDisposed())
        {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
	}

	class ImagePanel extends JPanel {
	    Image image;

	    public ImagePanel(Image image) {
	        this.image = image;
	    }

	    public void paintComponent(Graphics g) {
	        super.paintComponent(g);  // Paint background

	        // Draw image at its natural size first.
	        g.drawImage(image, 0, 0, null); //85x62 image

	    }
	}
}
