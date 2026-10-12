package org.delafer.xanderView.scale;

import com.mortennobel.imagescaling.MultiStepRescaleOp;
import net.sourceforge.jiu.apps.Strings;
import org.delafer.xanderView.common.ImageSize;
import org.delafer.xanderView.gui.config.ApplConfiguration;
import org.delafer.xanderView.scale.deprecated.ResizeOpenCL;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;


public class ScaleFactory  {


	//CV / OpenCV Quality (upscale): CV_LANCZOS4 > CV_BICUBIC > CV_LINEAR > CV_AREA > CV_NEAREST
	//CV / OpenCV Quality (downscale): CV_AREA > CV_LINEAR > CV_CUBIC >  CV_LANCZOS4 > CV_NEAREST
	//CV / OpenCV Speed:  CV_NEAREST > CV_LINEAR > CV_AREA > CV_BICUBIC > CV_LANCZOS4 (* = unstable speed)
	//While enlarging images, INTER_AREA work same as INTER_NEAREST,  INTER_AREA works better in image decimation and avoiding MOIRE pattern

	//only nearest is faster as opencv, with cpu
	//Hermite, Mitchel -> Hermite Sharp, but artifacts, Mitchel -> no artifacts, but very unsharp; speed same, slower as Lanczos
	//speed: AWT_2D_NEAREST > AWT_2D_BILLINEAR >  NOBEL_MULTISTEP > NOBEL_LANCZOS3

	public enum SCALER {
		AWT_2D_NEAREST          (1), //fastest, but very low quality
		AWT_2D_BILLINEAR        (2), //fastest for upscale with still good quality (2) ok for downscale, but only little bit faster as nobel multistep
		AWT_2D_BICUBIC 		    (3), //no usage, because it slower as Lanczos
		CV_LINEAR 			    (4), //hz
		CV_AREA 				(5), //good and fast for downscale
		CV_BICUBIC			    (6), //average performer, but still fast
		CV_LANCZOS4			    (7), //best for upscale, unstable
		NOBEL_HERMITE		    (8), //don't know, slower as lanczos
		NOBEL_MITCHEL		    (9), //don't know, slower as lanczos
		NOBEL_BICUBIC_HF		(10), //second best for upscale, but slower as Nobel Laczos
		NOBEL_LANCZOS3		    (11), //III best for upscale, and faster as BicubicHF
		NOBEL_MULTISTEP         (12); //good for downscale and little faster as lanczos and as fast as bilinear
		int id;
		SCALER(int id) { this.id = id; }
	}

	private static Map<Integer, SCALER> scalerIds = new HashMap<>(12, 1f);
	static {
		for (SCALER next : SCALER.values()) {
			scalerIds.put(next.id, next);
		}
	}

	private ScalerHelper scaler;
	/**
	 * Gets the single instance of ResourcesDR.
	 * @return single instance of ResourcesDR
	 */
	public static ScaleFactory instance() {
		return Holder.INSTANCE;
	}

	private ScaleFactory() {
		ApplConfiguration cfg = ApplConfiguration.instance();
		scaler = new ScalerHelper(cfg.get(ApplConfiguration.SCALER));
//		System.out.println("Used scaler: "+scaler);
	}

	public IResizer getInstanceByType(int type) {
		IResizer resizer = getInstanceByType0(type);
		return resizer;
	}

	//change to private again
	public IResizer getInstanceByType0(int type) {
		SCALER scaler = scalerIds.get(type);

		switch (scaler) {
		case CV_LINEAR:
			return ResizerNewOpenCV.instance(2);
		case CV_AREA:
			return ResizerNewOpenCV.instance(1);
		case CV_BICUBIC:
			return ResizerNewOpenCV.instance(3);
		case CV_LANCZOS4:
			return ResizerNewOpenCV.instance(4);
		case NOBEL_LANCZOS3:
			return new ResizerNobel(0);
		case NOBEL_BICUBIC_HF:
			return new ResizerNobel(1);
		case NOBEL_MITCHEL:
			return new ResizerNobel(3);
		case NOBEL_HERMITE:
			return new ResizerNobel(4);
		case AWT_2D_BICUBIC:
			return new ResizerJava2D(2);
		case AWT_2D_BILLINEAR:
			return new ResizerJava2D(1);
		case AWT_2D_NEAREST:
			return new ResizerJava2D(0);
		case NOBEL_MULTISTEP:
		default:
			return new IResizer() {
				public BufferedImage resize(BufferedImage sourceImage, int outputWidth, int outputHeight) {
					//System.out.println(String.format("resized to %s - %s", outputWidth, outputHeight));
					MultiStepRescaleOp msOp = new MultiStepRescaleOp(outputWidth, outputHeight);
					return msOp.filter(sourceImage, null);
				}
				public String name() { return "NobelMutlistep"; }
			};
		}
	}

	public IResizer getInstanceBySize(ImageSize size) {
		IResizer r =  getInstanceByType(scaler.getTypeBySize(size));
		return r;
	}


	public static IResizer instance(ImageSize size) {
		ScaleFactory factory = ScaleFactory.instance();
		return  factory.getInstanceBySize(size);
	}


	/**
	 * Lazy-loaded Singleton, by Bill Pugh *.
	 */
	private static final class Holder {
		/** The Constant INSTANCE. */
		private final static transient ScaleFactory INSTANCE = new ScaleFactory();
	}


//	public BufferedImage resize(BufferedImage sourceImage, int outputWidth, int outputHeight) {
//		return scaler.resize(sourceImage, outputWidth, outputHeight);
//	}
}
