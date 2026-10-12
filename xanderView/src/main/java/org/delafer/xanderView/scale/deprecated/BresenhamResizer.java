package org.delafer.xanderView.scale.deprecated;

import java.awt.Point;
import java.awt.image.*;

import org.delafer.xanderView.scale.IResizer;

public class BresenhamResizer implements IResizer {


	//Code originally written in 2007
	public  BufferedImage resize(BufferedImage original, int newWidth, int newHeight)
	    {
	        int[] rawInput = new int[original.getHeight() * original.getWidth()];

	        original.getRGB(0, 0, original.getWidth(), original.getHeight(), rawInput, 0, original.getWidth());

	        int[] rawOutput = new int[newWidth*newHeight];

	        // YD compensates for the x loop by subtracting the width back out
	        int YD = (original.getHeight() / newHeight) * original.getWidth() - original.getWidth();
	        int YR = original.getHeight() % newHeight;
	        int XD = original.getWidth() / newWidth;
	        int XR = original.getWidth() % newWidth;
	        int outOffset= 0;
	        int inOffset=  0;
	        int wOriginal = original.getWidth();

	        for (int y= newHeight, YE= 0; y > 0; y--) {
	            for (int x= newWidth, XE= 0; x > 0; x--) {
	                rawOutput[outOffset++]= rawInput[inOffset];
	                inOffset+=XD;
	                XE+=XR;
	                if (XE >= newWidth) {
	                    XE-= newWidth;
	                    inOffset++;
	                }
	            }
	            inOffset+= YD;
	            YE+= YR;
	            if (YE >= newHeight) {
	                YE -= newHeight;
	                inOffset+=wOriginal;
	            }
	        }


//	        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
//
//
//	        WritableRaster raster = (WritableRaster) image.getRaster();
//	        raster.getPixels(0, 0, newWidth , newHeight, rawOutput);

//	        BufferedImage image = new BufferedImage( newWidth, newHeight, BufferedImage.TYPE_INT_RGB );
//	        final int[] a = ( (DataBufferInt) image.getRaster().getDataBuffer() ).getData();
//	        System.arraycopy(rawOutput, 0, a, 0, rawOutput.length);

//	        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
//	        image.setRGB(0, 0, newWidth, newHeight, rawOutput, 0, rawOutput.length);
//	        Graphics2D g = image.createGraphics();
//	        g.drawImage(image, 0, 0, null);

	        int[] bitMasks = new int[]{0xFF0000, 0xFF00, 0xFF, 0xFF000000};
	        SinglePixelPackedSampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, newWidth , newHeight, bitMasks);
	        DataBufferInt db = new DataBufferInt(rawOutput, rawOutput.length);
	        WritableRaster wr = Raster.createWritableRaster(sm, db, new Point());
	        BufferedImage image = new BufferedImage(ColorModel.getRGBdefault(), wr, false, null);
	        return image;
	    }

	@Override
	public String name() {
		return null;
	}

}
