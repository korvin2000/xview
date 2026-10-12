package org.delafer.xanderView.orientation;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Rotator2D extends CommonRotator {

	//http://mindprod.com/jgloss/affinetransform.html
	//http://www.developer.com/net/vb/article.php/626051/Java-2D-Graphics-Simple-Affine-Transforms.htm

	public Rotator2D() {
	}

	@Override
	public BufferedImage flipVertical(BufferedImage image) {
		// Flip the image vertically
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -image.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		image = op.filter(image, null);
		return image;
	}

	@Override
	public BufferedImage flipHorizontal(BufferedImage image) {
		// Flip the image horizontally
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-image.getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		image = op.filter(image, null);
		return image;
	}


	private void affineOperation(AffineTransform tx, BufferedImage src, BufferedImage dst, int type) {
		if (type > 0) {
	        AffineTransformOp op = new AffineTransformOp(tx, type);
	        op.filter(src, dst);
		} else {
	        Graphics2D g = dst.createGraphics();
	        g.drawImage(src, tx, null);
	        g.dispose();
		}
	}

	public static BufferedImage rotateImage(BufferedImage image, int quadrants) {

	    int w0 = image.getWidth();
	    int h0 = image.getHeight();
	    int w1 = w0;
	    int h1 = h0;
	    int centerX = w0 / 2;
	    int centerY = h0 / 2;

	    if (!(quadrants % 2 == 0)) {
	        w1 = h0;
	        h1 = w0;
	    }

	    if (quadrants % 4 == 1) {
	        centerX = h0 / 2;
	        centerY = h0 / 2;
	    } else if (quadrants % 4 == 3) {
	        centerX = w0 / 2;
	        centerY = w0 / 2;
	    }

	    AffineTransform affineTransform = new AffineTransform();
	    affineTransform.setToQuadrantRotation(quadrants, centerX, centerY);
	    AffineTransformOp opRotated = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
	    BufferedImage transformedImage = new BufferedImage(w1, h1,
	            image.getType());
	    transformedImage = opRotated.filter(image, transformedImage);

	    return transformedImage;

	}

	@Override
	public BufferedImage rotateRight(final BufferedImage inputImage) {

		return rotateImage(inputImage, 1);

//		int w = inputImage.getWidth();
//		int h = inputImage.getHeight();
//		AffineTransform tx = AffineTransform.getQuadrantRotateInstance(1,  w / 2 , w / 2 );
//        BufferedImage dest = new BufferedImage(w, h, inputImage.getType());
//
//        int op = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
//        affineOperation(tx, inputImage, dest, op);
//
//        return dest;
	}

	@Override
	public BufferedImage rotateLeft(BufferedImage inputImage) {

		return rotateImage(inputImage, 3);

//		int w = inputImage.getWidth();
//		int h = inputImage.getHeight();
//		AffineTransform tx = AffineTransform.getQuadrantRotateInstance(3, w / 2, h / 2);
//        BufferedImage dest = new BufferedImage(w, h, inputImage.getType());
//
//        int op = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
//        affineOperation(tx, inputImage, dest, op);
//
//        return dest;
	}

	@Override
	public BufferedImage turnAbout(BufferedImage image ) {

		return rotateImage(image, 2);

//		// Flip the image vertically and horizontally; equivalent to rotating the image 180 degrees
//		AffineTransform tx = AffineTransform.getScaleInstance(-1, -1);
//		tx.translate(-image.getWidth(null), -image.getHeight(null));
//		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
//		image = op.filter(image, null);
//		return image;
	}

}
