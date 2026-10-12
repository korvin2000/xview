package org.delafer.xanderView.scale.deprecated;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.j7.commons.strings.Args;
import net.j7.commons.strings.StringUtils;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleOp;

public class Tester {

	public Tester() {
		// TODO Auto-generated constructor stub
	}

	public static BufferedImage resize(BufferedImage image, int width, int height, ResampleFilter filter) {
//		System.out.println("RESIZER "+this.getClass().getSimpleName());
		ResampleOp  resampleOp = new ResampleOp (width,height);
		resampleOp.setFilter(filter);
		resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.None);
		BufferedImage rescaledTomato = resampleOp.filter(image, null);

		return rescaledTomato;


	}

	//downscale - HermiteFilter (618), BiCubicHighFreqResponse (883) , Lanczos3Filter (1053)
	//upscale - Hermite, BiCubicHighFreqResponse
//	public static void main(String[] args) {
//
//		try {
//
//			List<ResampleFilter> filters = new ArrayList<ResampleFilter>();
//			filters.add(ResampleFilters.getBellFilter());
//			filters.add(ResampleFilters.getBiCubicFilter());
//			filters.add(ResampleFilters.getBiCubicHighFreqResponse());
//			filters.add(ResampleFilters.getBoxFilter());
//			filters.add(ResampleFilters.getBSplineFilter());
//			filters.add(ResampleFilters.getHermiteFilter());
//			filters.add(ResampleFilters.getLanczos3Filter());
//			filters.add(ResampleFilters.getMitchellFilter());
//			filters.add(ResampleFilters.getTriangleFilter());
//			BufferedImage bi = ImageIO.read(new File("e:\\good.bmp"));
//			double koeff = 1.4336d;
//			int wn = (int)Math.round((double)bi.getWidth() * koeff);
//			int hn = (int)Math.round((double)bi.getHeight() * koeff);
//			int iter = (int)Math.round(25  / (koeff * koeff));
//			int i = 0;
//			for (ResampleFilter next : filters) {
//				i++;
//				BufferedImage res = null;
//				long t1 = System.currentTimeMillis();
//				for (int j = 0; j < iter; j++) {
//					res = resize(bi, wn, hn, next);
//				}
//				long t2 = System.currentTimeMillis();
//				System.out.println(i);
//				String ext = "PNG";
//				ImageIO.write(res, ext, new File(Args.fill("e:/out/result_%1(%2).%3", StringUtils.fillString(""+(t2-t1),5,'0'), next.getClass().getSimpleName(), ext)));
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		System.out.println("Done");
//	}


	public static void main(String[] args) {

		try {

			List<ResampleFilter> filters = new ArrayList<ResampleFilter>();
//			filters.add(ResampleFilters.getBellFilter());
//			filters.add(ResampleFilters.getBiCubicFilter());
//			filters.add(ResampleFilters.getBiCubicHighFreqResponse());
//			filters.add(ResampleFilters.getBoxFilter());
//			filters.add(ResampleFilters.getBSplineFilter());
			filters.add(new MyHermiteFilter());
//			filters.add(ResampleFilters.getLanczos3Filter());
//			filters.add(ResampleFilters.getMitchellFilter());
//			filters.add(ResampleFilters.getTriangleFilter());
			BufferedImage bi = ImageIO.read(new File("e:\\good.bmp"));
			double koeff = 1.4336d;
			int wn = (int)Math.round((double)bi.getWidth() * koeff);
			int hn = (int)Math.round((double)bi.getHeight() * koeff);
			int iter = (int)Math.round(25  / (koeff * koeff));
			int i = 0;
			i++;
			BufferedImage res = null;
			for (ResampleFilter next : filters) {
				long t1 = System.currentTimeMillis();
				res = resize(bi, wn, hn, next);
				long t2 = System.currentTimeMillis();
				System.out.println(i);
				String ext = "PNG";
				ImageIO.write(res, ext, new File(Args.fill("e:/out/xxxresult_%1(%2).%3", StringUtils.fillString(""+(t2-t1),5,'0'), "Scalr", ext)));
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done");
	}

	static class MyHermiteFilter implements ResampleFilter
	{
		public float getSamplingRadius() {
			return 1.0f;
		}

		public float apply(float value)
		{

			System.out.println(value);
			if (value < 0.0f)
			{
				value = - value;
			}
			if (value < 1.0f)
			{
				return (2.0f * value - 3.0f) * value * value + 1.0f;
			}
			else
			{
				return 0.0f;
			}
		}

		public String getName() {
			return "BSpline";
		}
	}

}
