package org.delafer.xanderView.scale.others;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.*;

import org.delafer.xanderView.scale.ResizerBase;

public class Graph2DResizer extends ResizerBase {

	public static final ResizerBase instance = new Graph2DResizer(-1);


	static Object[] INTERPOLATION = new Object[] {RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, RenderingHints.VALUE_INTERPOLATION_BILINEAR, RenderingHints.VALUE_INTERPOLATION_BICUBIC};
	static Object[] RENDERING = new Object[] {RenderingHints.VALUE_RENDER_SPEED, RenderingHints.VALUE_RENDER_QUALITY};
	static Object[] ANTIALIASING = new Object[] {RenderingHints.VALUE_ANTIALIAS_OFF, RenderingHints.VALUE_ANTIALIAS_ON};
	static Object[] DITHERING = new Object[] {RenderingHints.VALUE_DITHER_DISABLE, RenderingHints.VALUE_DITHER_ENABLE};

	static List<Object[]> filters = new ArrayList<Object[]>();
	static Map<Object, String> names = new HashMap<Object, String>();
	static {
		int i = 0;

		names.put(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, "_Nearest");
		names.put(RenderingHints.VALUE_INTERPOLATION_BILINEAR, "_Bilinear");
		names.put(RenderingHints.VALUE_INTERPOLATION_BICUBIC, "_Bicubic");
		names.put(RenderingHints.VALUE_RENDER_SPEED, "_LQ");
		names.put(RenderingHints.VALUE_RENDER_QUALITY, "_HQ");
		names.put(RenderingHints.VALUE_ANTIALIAS_OFF, "_AA");
		names.put(RenderingHints.VALUE_ANTIALIAS_ON, "_NAA");
		names.put(RenderingHints.VALUE_DITHER_ENABLE, "_dither");
		names.put(RenderingHints.VALUE_DITHER_DISABLE, "");

		for (Object nextD : DITHERING) {
			for (Object nextA : ANTIALIASING) {
				for (Object nextR : RENDERING) {
					for (Object nextI : INTERPOLATION) {
						filters.add(new Object[]{nextI, nextR, nextA, nextD});
					}
				}
			}

		}
//		System.out.println(filters.size());
	}



	private Object[] filterCfg;
	private int filterIdx;

	public Graph2DResizer as(int filterI) {
		return new Graph2DResizer(filterI);
	}

	public Graph2DResizer(int filterI) {
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
		resultGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, filterCfg[1]);
		resultGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, filterCfg[2]);
		resultGraphics.setRenderingHint(RenderingHints.KEY_DITHERING, filterCfg[3]);
		resultGraphics.drawImage(src, 0, 0, w, h, null);

		// Just to be clean, explicitly dispose our temporary graphics object
		resultGraphics.dispose();
		return result;
	}

	public int getMaxFilters() {
		return 24;
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


//	public static void main(String[] args) {
//		Graph2DResizer r = new Graph2DResizer(0);
//		ResizerBase b = r;
//		System.out.println(r.name());
//	}

	@Override
	public String toString() {
		return String.format("Graph2DResizer [%s]", name());
	}
}
