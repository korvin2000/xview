package org.delafer.xanderView.scale.others;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.*;

import org.delafer.xanderView.scale.ResizerBase;

public class BufferedImageResizer extends ResizerBase {

	public static final ResizerBase instance = new BufferedImageResizer(-1);


	static List<Integer> filters = new ArrayList<Integer>();

	static Map<Integer, String> names = new HashMap<Integer, String>();

	static {
		filters.add(java.awt.Image.SCALE_FAST);
		filters.add(java.awt.Image.SCALE_REPLICATE);
		filters.add(java.awt.Image.SCALE_AREA_AVERAGING);

		names.put(java.awt.Image.SCALE_FAST, "FAST");
		names.put(java.awt.Image.SCALE_REPLICATE, "REPL");
		names.put(java.awt.Image.SCALE_AREA_AVERAGING, "AREA_AVG");

	}

	private int filterIdx;


	private Integer scaler;

	public BufferedImageResizer as(int filterI) {
		return new BufferedImageResizer(filterI);
	}

	public BufferedImageResizer(int filterI) {
		if (filterI < 0) return ;
		//1-24
		this.filterIdx = filterI;
		this.scaler = filters.get(filterI);
	}


	public int getMaxFilters() {
		return 3;
	}

	@Override
	public String name() {
		return "AwtImgScale_"+names.get(scaler);
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
			Image result = sourceImage.getScaledInstance(outputWidth, outputHeight,  scaler.intValue());
			return toBufferedImage(result);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}


	public static void main(String[] args) {
//		 ResizerBase r = ScalrResizer.instance;
		ResizerBase r = new BufferedImageResizer(2);
		ResizerBase b = r;
		System.out.println(r.name());
	}

	@Override
	public int current() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		return String.format("BufferedImageResizer [%s]", name());
	}
}
