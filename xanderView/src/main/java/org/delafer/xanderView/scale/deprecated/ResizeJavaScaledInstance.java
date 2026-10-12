package org.delafer.xanderView.scale.deprecated;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.delafer.xanderView.scale.IResizer;

public class ResizeJavaScaledInstance implements IResizer {

	public ResizeJavaScaledInstance() {
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }
	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}

	@Override
	public BufferedImage resize(BufferedImage sourceImage, int outputWidth, int outputHeight) {

		try {
			Image result = sourceImage.getScaledInstance(outputWidth, outputHeight,  java.awt.Image.SCALE_AREA_AVERAGING);
			return toBufferedImage(result);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	public String name() {
		return this.getClass().getSimpleName();
	}

}
