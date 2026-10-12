package org.delafer.xanderView.scale.test;

import org.delafer.xanderView.scale.*;
import org.delafer.xanderView.scale.others.BufferedImageResizer;
import org.delafer.xanderView.scale.others.CustomScalers;
import org.delafer.xanderView.scale.others.Graph2DResizer;
import org.delafer.xanderView.scale.others.ScalrResizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class TestUpscale2 {

	transient static TreeMap<Long, Set<String>> results = new TreeMap<>();
	static Set<String> byKey(Long key) {
		Set<String> r = results.get(key);
		if (r == null) {
			r = new TreeSet<>();
			results.put(key, r);
		}
		return r;
	}

	public static void main(String[] args) {
		try {

			BufferedImage image = ImageIO.read(new File("A:\\target\\src\\upscale3.jpg"));
			System.out.println(image);

			for (int i = 1; i <= 11 ; i++) {
				IResizer r =  ScaleFactory.instance().getInstanceByType(i);
				long hc = 0;
				waitAndGc();
				long t1 = System.currentTimeMillis();
//				for (int j = 0; j < 7; j++) {
					BufferedImage bi = r.resize(image, 3052, 2160);
					hc += System.identityHashCode(bi);
//				}
				long t2 = System.currentTimeMillis();
				System.out.println(String.format("Method: %s  time: %s", i, (t2-t1)));
				byKey((t2-t1)).add(String.format("%s(%s)", r, i));
				ImageIO.write(bi, "png", new File(String.format("A:\\upscale1\\%s.png", r)));

			}

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

			for (ResizerBase filter : filters) {

				long hc = 0;
				waitAndGc();
				long t1 = System.currentTimeMillis();
//				for (int j = 0; j < 7; j++) {
					BufferedImage bi = filter.resize(image, 3052, 2160);
					hc += System.identityHashCode(bi);
//				}
				long t2 = System.currentTimeMillis();
				System.out.println(String.format("Method: %s  time: %s", filter, (t2-t1)));
				byKey((t2-t1)).add(String.format("%s", filter));
				ImageIO.write(bi, "png", new File(String.format("A:\\upscale1\\%s.png", filter)));
			}
			for (int i = 0; i < 4; i++) {
				System.out.println();
			}
			System.out.println("===========================================================");
			for (int i = 0; i < 4; i++) {
				System.out.println();
			}

			for (Map.Entry<Long, Set<String>> next : results.entrySet()) {
				Set<String> set = next.getValue();
				for (String nextS : set) {
					System.out.println(String.format("%s = %s", nextS, next.getKey()));
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void waitAndGc() {
//		System.gc();System.gc();System.gc();
//		System.runFinalization();
//		try {
//			Thread.sleep(750);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		System.gc();
//		System.runFinalization();
//		System.gc();
//		try {
//			Thread.sleep(750);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}
}
