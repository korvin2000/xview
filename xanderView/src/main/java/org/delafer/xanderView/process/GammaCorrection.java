package org.delafer.xanderView.process;

import java.awt.image.BufferedImage;

/**
 * Gamma correction algorithm
 * <p/>
 * Author: Bostjan Cigan (http://zerocool.is-a-geek.net)
 */
public class GammaCorrection {
	public static BufferedImage gammaCorrection(BufferedImage original, double gamma) {
		int alpha, red, green, blue;
		int newPixel;
		double gamma_new = 1d / gamma;
		final int[] gamma_LUT = gamma_LUT(gamma_new);
		final BufferedImage gamma_cor = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
		for (int j = original.getHeight()-1; j >= 0; j--)
			for (int i = original.getWidth()-1; i >= 0; i--) {
				// Get pixels by R, G, B
				alpha = getColor(original.getRGB(i, j), 0);
				red = getColor(original.getRGB(i, j), 1);
				green = getColor(original.getRGB(i, j), 2);
				blue = getColor(original.getRGB(i, j), 3);
				red = gamma_LUT[red];
				green = gamma_LUT[green];
				blue = gamma_LUT[blue];
				// Return back to original format
				newPixel = colorToRGB(alpha, red, green, blue);
				// Write pixels into image
				gamma_cor.setRGB(i, j, newPixel);
			}
		return gamma_cor;
	}

	public static final int getColor(int rgb, int mod) {
		rgb = 0xff000000 | rgb;
		switch (mod) {
		default:
		case 0:
			return (rgb >> 24) & 0xff;
		case 1:
			return (rgb >> 16) & 0xFF;
		case 2:
			return (rgb >> 8) & 0xFF;
		case 3:
			return (rgb) & 0xFF;
		}
	}

	// Create the gamma correction lookup table
	private static final int[] gamma_LUT(double gamma_new) {
		final int[] gamma_LUT = new int[256];
		for (int len = gamma_LUT.length-1, i = len; i >= 0; i--)
			gamma_LUT[i] = (int) (255d * Math.pow(i / 255d, gamma_new));
		return gamma_LUT;
	}

	// Convert R, G, B, Alpha to standard 8 bit
	private static final int colorToRGB(final int alpha,final  int red, final int green, final int blue) {
		int newPixel = 0;
		newPixel += alpha;
		newPixel = newPixel << 8;
		newPixel += red;
		newPixel = newPixel << 8;
		newPixel += green;
		newPixel = newPixel << 8;
		newPixel += blue;
		return newPixel;
	}
}
