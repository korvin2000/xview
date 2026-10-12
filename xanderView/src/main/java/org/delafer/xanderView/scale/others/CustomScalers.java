package org.delafer.xanderView.scale.others;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.coobird.thumbnailator.Thumbnails;

import org.delafer.xanderView.scale.ResizerBase;
import org.delafer.xanderView.scale.ResizerJAI;
import org.delafer.xanderView.scale.deprecated.BresenhamResizer;
import org.delafer.xanderView.scale.deprecated.ImageScaler;
import org.delafer.xanderView.scale.deprecated.ResizeOpenCL;

import com.mortennobel.imagescaling.MultiStepRescaleOp;

public class CustomScalers extends ResizerBase {

	public static final ResizerBase instance = new CustomScalers(-1);

	public final static int FILTER_CAMBELL_SCALER = 0;
	public final static int FILTER_NOBEL_MULTISTEPRESCALER  = 1;
	public final static int FILTER_NOBEL_MULTISTEPRESCALER_HQ  = 2;
	public final static int FILTER_BRESENHAM = 3;

	public final static int FILTER_THUMBNAILATOR = 4;

	public final static int FILTER_GPU = 5;
	public final static int FILTER_JAI = 6;
//	public final static int FILTER_OPENCV = 7;

	int filter;

	static Map<Integer, String> names = new HashMap<Integer, String>();
	static {

		names.put(FILTER_CAMBELL_SCALER, "Cambel_Mutlistep");
		names.put(FILTER_NOBEL_MULTISTEPRESCALER, "Nobel_Mutlistep");
		names.put(FILTER_NOBEL_MULTISTEPRESCALER_HQ, "Nobel_MutlistepHQ");
		names.put(FILTER_BRESENHAM, "Bresenham");

		names.put(FILTER_THUMBNAILATOR, "thumbnailtr");
		//names.put(FILTER_JAI, "JAI");
		//names.put(FILTER_GPU, "OpenCl");
		//names.put(FILTER_OPENCV, "openCV");
	}

	@Override
	public int hashCode() {
		return this.getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (!(obj instanceof CustomScalers)) return false;
		CustomScalers other = (CustomScalers) obj;

		return this.getClass().getName().equals(other.getClass().getName());
	}

	public CustomScalers as(int filterI) {
		return new CustomScalers(filterI);
	}

	public CustomScalers(int filter) {
		if (filter < 0 )return ;
		this.filter = filter;
	}

	@Override
	public BufferedImage resize(BufferedImage src, int w, int h) {
		switch (filter) {
		case FILTER_GPU:
			return ResizeOpenCL.instance().resize(src, w, h);
//			return src;
		case FILTER_CAMBELL_SCALER:
			ImageScaler sc = new ImageScaler();
			return sc.scaleImage(src, new java.awt.Dimension(w,h));
		case FILTER_NOBEL_MULTISTEPRESCALER:
			MultiStepRescaleOp  msOp = new MultiStepRescaleOp(w, h);
			return msOp.filter(src, null);
		case FILTER_NOBEL_MULTISTEPRESCALER_HQ:
			MultiStepRescaleOp  msOpHQ = new MultiStepRescaleOp(w, h, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			return msOpHQ.filter(src, null);
		case FILTER_BRESENHAM:
			BresenhamResizer rr = new BresenhamResizer();
			return rr.resize(src, w, h);
		case FILTER_THUMBNAILATOR:
			try {
				BufferedImage thumbnail = Thumbnails.of(src).size(w,h).asBufferedImage();
				return thumbnail;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		case FILTER_JAI:
			ResizerJAI rjai = new ResizerJAI();
			return rjai.resize(src, w, h);
//		case FILTER_OPENCV:
//			return ResizerJavaCV.instance().resize(src, w, h);
		default:
			break;
		}
		return null;
	}

	@Override
	public String name() {
		return names.get(filter);
	}

	@Override
	public int getMaxFilters() {
		return 4;
	}

	@Override
	public int current() {
		return filter;
	}

	@Override
	public String toString() {
		return String.format("CustomScaler [%s]", name());
	}

}
