package org.delafer.xanderView.orientation;

import java.awt.image.BufferedImage;

import no.nixx.opencl.ImageRotator;
import no.nixx.opencl.ImageRotator.Rotation;

public class RotatorGPU extends CommonRotator {

	private ImageRotator rotator;

	public RotatorGPU() {
		rotator = new ImageRotator();
	}

	public BufferedImage flipVertical(BufferedImage inp) {
		// TODO Auto-generated method stub
		return null;
	}

	public BufferedImage flipHorizontal(BufferedImage inp) {
		// TODO Auto-generated method stub
		return null;
	}

	public BufferedImage rotateRight(BufferedImage inp) {
		return rotator.rotate(inp, Rotation.CW_90);
	}

	public BufferedImage rotateLeft(BufferedImage inp) {
		return rotator.rotate(inp, Rotation.CCW_90);
	}

	@Override
	public BufferedImage turnAbout(BufferedImage inp) {
		return rotator.rotate(inp, Rotation.FLIP);
	}

}
