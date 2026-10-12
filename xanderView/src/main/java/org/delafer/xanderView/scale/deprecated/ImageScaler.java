 package org.delafer.xanderView.scale.deprecated;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ImageScaler {


	//info http://stackoverflow.com/questions/26534380/fast-java2d-hardware-scaling-of-images
	//https://code.google.com/p/thumbnailator/wiki/Comparison
	//https://code.google.com/p/javacl/wiki/UsingImages
	//https://github.com/oddbjornkvalsund/opencl-img/blob/master/src/main/java/no/nixx/opencl/ResizeOpenCL.java
	//https://github.com/oddbjornkvalsund/opencl-img
	//http://blog.nobel-joergensen.com/2008/12/20/downscaling-images-in-java/
	//http://code.google.com/p/java-image-scaling/
	//test
	//JAI
	//Graphics 2D
	//AffineTransform - NEAREST_NEIGHBOUR

    public BufferedImage scaleImage(BufferedImage img, Dimension d) {
        img = scaleByHalf(img, d);
        img = scaleExact(img, d);
        return img;
    }

    private BufferedImage scaleByHalf(BufferedImage img, Dimension d) {
        int w = img.getWidth();
        int h = img.getHeight();
        float factor = getBinFactor(w, h, d);

        // make new size
        w *= factor;
        h *= factor;
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }

    private BufferedImage scaleExact(BufferedImage img, Dimension d) {
        float factor = getFactor(img.getWidth(), img.getHeight(), d);

        // create the image
        int w = (int) (img.getWidth() * factor);
        int h = (int) (img.getHeight() * factor);
        BufferedImage scaled = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }

    float getBinFactor(int width, int height, Dimension dim) {
        float factor = 1;
        float target = getFactor(width, height, dim);
        if (target <= 1) { while (factor / 2 > target) { factor /= 2; }
        } else { while (factor * 2 < target) { factor *= 2; }         }
        return factor;
    }

    float getFactor(int width, int height, Dimension dim) {
        float sx = dim.width / (float) width;
        float sy = dim.height / (float) height;
        return Math.min(sx, sy);
    }
}