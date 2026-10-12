package org.delafer.xanderView.scale.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.j7.commons.strings.Args;
import net.j7.commons.strings.StringUtils;

import org.delafer.xanderView.common.ImageSize;
import org.delafer.xanderView.file.entry.HelperFS;
import org.delafer.xanderView.orientation.OrientationCommons;
import org.delafer.xanderView.scale.*;
import org.delafer.xanderView.scale.others.BufferedImageResizer;
import org.delafer.xanderView.scale.others.CustomScalers;
import org.delafer.xanderView.scale.others.Graph2DResizer;
import org.delafer.xanderView.scale.others.ScalrResizer;
import org.libjpegturbo.turbojpeg.TJDecompressor;
import org.libjpegturbo.turbojpeg.TJScalingFactor;

public class TesterQuality {
	// HQ_NAA_dither
	public TesterQuality() {
		// TODO Auto-generated constructor stub
	}

	// Nearest HQ AA Dither
	// Bilinear HQ AA Dither

	// downscale - HermiteFilter (618), BiCubicHighFreqResponse (883) ,
	// Lanczos3Filter (1053)
	// upscale - Hermite, BiCubicHighFreqResponse

	public static BufferedImage getImage(String name, int type) {
		try {
			byte[] a = HelperFS.readData(name, -1);

			TJScalingFactor sf = new TJScalingFactor(1, 1);
			TJDecompressor tjd = new TJDecompressor(a);
			int width = sf.getScaled(tjd.getWidth());
			int height = sf.getScaled(tjd.getHeight());
			BufferedImage img = tjd.decompress8(type);
			return img;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {

		try {

			ResizerBase[] rs = new ResizerBase[] {
					ResizerNobel.instance,
					Graph2DResizer.instance,
					ScalrResizer.instance,
					BufferedImageResizer.instance,
					CustomScalers.instance,
					ResizerOpenCV.instance(1)
					};

			List<ResizerBase> filters = new ArrayList<ResizerBase>();

			for (ResizerBase next : rs) {
				int max = next.getMaxFilters();
				for (int i = 0; i < max; i++) {
					ResizerBase filter = next.as(i);
					filters.add(filter);
				}
			}

			String[] bases = new String[] { "qtest2" };
			for (String base : bases) {

				BufferedImage bi1 = getImage("e:\\" + base + ".jpg", BufferedImage.TYPE_3BYTE_BGR);// ImageIO.read(new
				BufferedImage bi2 = getImage("e:\\" + base + ".jpg", BufferedImage.TYPE_INT_RGB);
				BufferedImage bi = null;										// File("e:\\big.jpg"));
				System.out.println("Base: "+base);

				ImageSize size = OrientationCommons.getNewSize(bi1.getWidth(), bi1.getHeight(), 1920, 1080);

				float koef = 1.452739f;
				int wn = (int) ((float)bi1.getWidth() * koef);
				int hn = (int) ((float)bi1.getHeight() * koef);
				int iter = 10;

				int i = 0;
				for (ResizerBase next : filters) {
					i++;
					BufferedImage res = null;

					if (CustomScalers.instance.equals(next) && next.current() == CustomScalers.FILTER_GPU) {
						bi = bi2;
					} else {
						bi = bi1;

					}

					res = next.resize(bi, wn, hn);
					long t1 = System.currentTimeMillis();
					for (int j = 0; j < iter; j++) {
						res = next.resize(bi, wn, hn);
					}
					long t2 = System.currentTimeMillis();

					for (int j = 0; j < 4; j++) {
						System.gc();
						System.runFinalization();
						Thread.currentThread().sleep(40);
						Runtime.getRuntime().gc();
						Thread.currentThread().sleep(40);
					}
					Thread.currentThread().sleep(150);
					System.out.println(i);

					long r1 = Math.round((((double) (t2 - t1)) / 3.0d));

					String ext = "PNG";
					ImageIO.write(
							res,
							ext,
							new File(Args.fill("e:/out2/" + base + "/result_%1(%2)$.%3",
									StringUtils.fillString("" + (r1), 5, '0'), next.name(), ext)));

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done");
	}

}
