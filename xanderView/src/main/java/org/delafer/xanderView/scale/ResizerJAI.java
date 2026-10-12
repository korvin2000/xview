package org.delafer.xanderView.scale;

import java.awt.image.BufferedImage;

//import javax.media.jai.*;
//import javax.media.jai.operator.ScaleDescriptor;

public class ResizerJAI implements IResizer {

	static
	{
	System.setProperty("com.sun.media.jai.disableMediaLib", "true");
	}
	@Override
	public BufferedImage resize(BufferedImage image, int w, int h) {
//		if (image == null) return image;
//        PlanarImage pi = new RenderedImageAdapter(image);
//        int wo = image.getWidth();
//        int ho = image.getHeight();
//        float scale1 = (float)w / (float)wo;
//        float scale2 = (float)h / (float)ho;
//        scale1 = min(scale1, scale2);
//
//        RenderedOp op = ScaleDescriptor.create(pi, scale1, scale1, 0f, 0f, Interpolation.getInstance(Interpolation.INTERP_BICUBIC), null);
//        return op.getAsBufferedImage();
		return null;

	}

	private float min(float a, float b) {
		return a >= b ? a : b;
	}

	@Override
	public String name() {
		return this.getClass().getSimpleName();
	}

}
