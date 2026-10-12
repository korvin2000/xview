package org.delafer.xanderView.scale.test;

import org.delafer.xanderView.scale.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class TestOpenCvResizer {

	public static void main(String[] args) {
		try {

			BufferedImage image = ImageIO.read(new File("A:\\target\\src\\upscale2.jpg"));
//			System.out.println(image);

			int filtersCnt = ResizerOpenCV.instance(1).getMaxFilters();
			double average = 0d;

			ResizerOpenCV resTmp = ResizerOpenCV.instance(0);
			BufferedImage biTmp = resTmp.resize(image, 3609/2, 2160/2);
			System.out.println(biTmp);
			waitAndGc();waitAndGc();waitAndGc();waitAndGc();

			for (int ij = 0; ij <= filtersCnt; ij++) {

				long t1 = System.nanoTime();
				BufferedImage bi = null;
				ResizerOpenCV res = null;
				long h  = 0;
				int i = ij;
				if (i == filtersCnt) { i = 0;}

				for (int j = 6; j > 0; j--) {
					res = ResizerOpenCV.instance(i);
					for (int k = 1; k <= 2 ; k++) {
						bi = null;
						bi = res.resize(image, 3609*j/6, 2160*j/6);
						h += System.identityHashCode(bi);
						h += bi.getColorModel().getPixelSize();
					}
				}

				long t2 = System.nanoTime();
				double r = (t2 - t1) / 100000D;
				if (ij > 0) {
					average += r;
				}
				System.out.println(String.format("Method: %s  time: %.1f", res.toString(), r));
				ImageIO.write(bi, "png", new File(String.format("A:\\opencv\\%s.png", res.toString())));

			}
			average = average  / (double) filtersCnt;
			System.out.println(String.format("average time: %.1f",  average));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private static void waitAndGc() {
		System.gc();System.gc();System.gc();
		System.runFinalization();
		try {
			Thread.sleep(750);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.gc();
		System.runFinalization();
		System.gc();
		try {
			Thread.sleep(750);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
