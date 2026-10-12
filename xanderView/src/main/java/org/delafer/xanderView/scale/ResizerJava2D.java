package org.delafer.xanderView.scale;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.*;

import org.delafer.xanderView.scale.ResizerBase;

public class ResizerJava2D extends ResizerBase {

	public static final ResizerBase instance = new ResizerJava2D(-1);


	static Object[] INTERPOLATION = new Object[] {
		RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
		RenderingHints.VALUE_INTERPOLATION_BILINEAR,
		RenderingHints.VALUE_INTERPOLATION_BICUBIC
		};


	static List<Object[]> filters = new ArrayList<Object[]>();
	static Map<Object, String> names = new HashMap<Object, String>();
	static {

		names.put(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, "_Nearest");
		names.put(RenderingHints.VALUE_INTERPOLATION_BILINEAR, "_Bilinear");
		names.put(RenderingHints.VALUE_INTERPOLATION_BICUBIC, "_Bicubic");


		for (Object nextI : INTERPOLATION) {
			filters.add(new Object[]{nextI});
		}

//		System.out.println(filters.size());
	}



	private Object[] filterCfg;
	private int filterIdx;

	public ResizerJava2D as(int filterI) {
		return new ResizerJava2D(filterI);
	}

	public ResizerJava2D(int filterI) {
		if (filterI < 0) return ;
		//1-24
		this.filterIdx = filterI;
		this.filterCfg = filters.get(filterI);
	}

	@Override
	public BufferedImage resize(BufferedImage src, int w, int h) {
		BufferedImage result =  new BufferedImage(w,h,src.getType());
		Graphics2D resultGraphics = result.createGraphics();

		// Scale the image to the new buffer using the specified rendering hint.
		resultGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, filterCfg[0]);
		resultGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		resultGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		resultGraphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		resultGraphics.drawImage(src, 0, 0, w, h, null);

		// Just to be clean, explicitly dispose our temporary graphics object
		resultGraphics.dispose();
		return result;
	}

	public int getMaxFilters() {
		return 3;
	}

	@Override
	public String name() {
		StringBuilder sb = new StringBuilder();
		for (Object object : filterCfg) {
			sb.append(names.get(object));
		}
		return "java2D"+sb.toString();
	}

	@Override
	public int current() {
		return filterIdx;
	}

	public static void main(String[] args) {
		int max = ResizerJava2D.instance.getMaxFilters();
		for (int i = 0; i < max; i++) {
			ResizerBase rs = new ResizerJava2D(i);
			System.out.println("["+i+"]="+rs.name());
		}

	}


	@Override
	public String toString() {
		return String.format("ResizerJava2D [%s]", name());
	}

}
