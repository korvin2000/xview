package org.delafer.xanderView.scale.test;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import net.j7.commons.utils.Metrics;

import org.delafer.xanderView.common.ImageSize;
import org.delafer.xanderView.file.entry.HelperFS;
import org.libjpegturbo.turbojpeg.TJDecompressor;
import org.libjpegturbo.turbojpeg.TJScalingFactor;

import com.mortennobel.imagescaling.*;

public class TesterSingle {
	//HQ_NAA_dither
	public TesterSingle() {
		// TODO Auto-generated constructor stub
	}

	//Nearest HQ AA Dither
	//Bilinear HQ AA Dither

	//downscale - HermiteFilter (618), BiCubicHighFreqResponse (883) , Lanczos3Filter (1053)
	//upscale - Hermite, BiCubicHighFreqResponse

	public static BufferedImage getImage(String name) {
		try {
			byte[] a = HelperFS.readData(name, -1);

			TJScalingFactor sf =  new TJScalingFactor(1, 1);
			TJDecompressor tjd = new TJDecompressor(a);
			int width = sf.getScaled(tjd.getWidth());
			int height = sf.getScaled(tjd.getHeight());
			BufferedImage img = tjd.decompress8(BufferedImage.TYPE_INT_RGB);
			return img;
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedImage resize(BufferedImage src, int w, int h) {
		ResampleOpSlow  resampleOp = new ResampleOpSlow(w,h);
		resampleOp.setFilter(ResampleFilters.getHermiteFilter());
		resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.None);
		BufferedImage rescaledTomato = resampleOp.filter(src, null);
		return rescaledTomato;
	}

	public static  BufferedImage resizeFaster(BufferedImage src, int w, int h) {
		ResampleOp  resampleOp = new ResampleOp (w,h);
		resampleOp.setFilter(ResampleFilters.getHermiteFilter());
		resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.None);
		BufferedImage rescaledTomato = resampleOp.filter(src, null);
		return rescaledTomato;
	}

	public static void main(String[] args) {

		try {
			String base = "10mp";
			BufferedImage bi = getImage("f:\\todownscale.jpg");//ImageIO.read(new File("e:\\big.jpg"));


			ImageSize size = new ImageSize(838,521);
					//OrientationCommons.getNewSize(bi.getWidth(), bi.getHeight(), 1920, 1080);

			int wn =size.width();
			int hn = size.height();
			   resize(bi, wn, hn);
			   resizeFaster(bi, wn, hn);

			   System.out.println("rewarning");

				Metrics m = Metrics.start();
				System.gc();System.gc();System.gc();Thread.currentThread().sleep(2000);
				BufferedImage ress = null;
				for (int i = 0; i < 70; i++) {
					ress = resize(bi, wn, hn);
				}
				m.measure("slow");
				System.gc();System.gc();System.gc();Thread.currentThread().sleep(2000);
				BufferedImage resf = null;
				for (int i = 0; i < 70; i++) {
					resf = resizeFaster(bi, wn, hn);
				}
				m.measure("fast");
				String ext = "PNG";
				ImageIO.write(ress, ext, new File("C:\\Projects\\compareImage\\ImageMagick\\000res_orig.png"));
				ImageIO.write(resf, ext, new File("C:\\Projects\\compareImage\\ImageMagick\\000res_fast.png"));



		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done");
	}

}
