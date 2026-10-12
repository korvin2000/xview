package org.delafer.xanderView.orientation;

import java.awt.image.BufferedImage;

import no.nixx.opencl.ImageRotator;
import no.nixx.opencl.ImageRotator.Rotation;

public class RotatorCPU extends CommonRotator {


	public RotatorCPU() {
	}

	@Override
	public BufferedImage flipVertical(BufferedImage inp) {
		// TODO Auto-generated method stub
		Rotator2D dd = new Rotator2D();
		return dd.flipVertical(inp);
	}

	@Override
	public BufferedImage flipHorizontal(BufferedImage inp) {
		Rotator2D dd = new Rotator2D();
		return dd.flipHorizontal(inp);
	}

	@Override
	public BufferedImage rotateRight(final BufferedImage inputImage) {
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		final BufferedImage returnImage = new BufferedImage(height--, width--, inputImage.getType());
		int x, y;
		for (x = width; x >= 0; x--) {
			for (y = height; y >= 0; y--) {
				returnImage.setRGB(height - y, x, inputImage.getRGB(x, y));
				// Again check the Picture for better understanding
			}
		}
		return returnImage;
	}

	@Override
	public BufferedImage rotateLeft(BufferedImage inputImage) {
		// The most of code is same as before
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		BufferedImage returnImage = new BufferedImage(height, width, inputImage.getType());
		//We have to change the width and height because when you rotate the image by 90 degree, the
		//width is height and height is width <img src='http://forum.codecall.net/public/style_emoticons/<#EMO_DIR#>/smile.png' class='bbc_emoticon' alt=':)' />

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				returnImage.setRGB(y, width - x - 1, inputImage.getRGB(x, y));
				// Again check the Picture for better understanding
			}
		}
		return returnImage;
	}

	@Override
	public BufferedImage turnAbout(BufferedImage inputImage ) {

		//We use BufferedImage because it’s provide methods for pixel manipulation
		int width = inputImage.getWidth(); //the Width of the original image
		int height = inputImage.getHeight();//the Height of the original image

		BufferedImage returnImage = new BufferedImage( width, height, inputImage.getType()  );
		//we created new BufferedImage, which we will return in the end of the program
		//it set up it to the same width and height as in original image
		// inputImage.getType() return the type of image ( if it is in RBG, ARGB, etc. )

		for( int x = 0; x < width; x++ ) {
			for( int y = 0; y < height; y++ ) {
				returnImage.setRGB( width - x - 1, height - y - 1, inputImage.getRGB( x, y  )  );
			}
		}
		//so we used two loops for getting information from the whole inputImage
		//then we use method setRGB by whitch we sort the pixel of the return image
		//the first two parametres is the X and Y location of the pixel in returnImage and the last one is the //source pixel on the inputImage
		//why we put width – x – 1 and height –y – 1 is hard to explain for me, but when you rotate image by //180degree the pixel with location [0, 0] will be in [ width, height ]. The -1 is for not to go out of
		//Array size ( remember you always start from 0 so the last index is lower by 1 in the width or height
		//I enclose Picture for better imagination  ... hope it help you
		return returnImage;
		//and the last return the rotated image
	}

}
