package org.delafer.xanderView.scale.deprecated;

import java.awt.image.BufferedImage;

import org.delafer.xanderView.scale.IResizer;

public class ResizerFastAwt2D implements IResizer{


	public BufferedImage resize(BufferedImage image, int width, int height) {
//		System.out.println("RESIZER "+this.getClass().getSimpleName());
		return  Scalr.resize(image, Scalr.Method.BALANCED, Scalr.Mode.BEST_FIT_BOTH,
				width, height, Scalr.OP_ANTIALIAS);
	}

	public String name() {
		return this.getClass().getSimpleName();
	}

}
