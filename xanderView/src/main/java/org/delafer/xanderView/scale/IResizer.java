package org.delafer.xanderView.scale;

import java.awt.image.BufferedImage;

public interface IResizer {

	public BufferedImage resize(BufferedImage sourceImage, int outputWidth, int outputHeight);

	public String name();
}
