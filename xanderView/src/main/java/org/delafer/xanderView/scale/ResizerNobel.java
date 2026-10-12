package org.delafer.xanderView.scale;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.mortennobel.imagescaling.*;

public class ResizerNobel extends ResizerBase {

	public static final ResizerBase instance = new ResizerNobel(-1);

	static List<ResampleFilter> filters = new ArrayList<ResampleFilter>(9);

	static {
		filters.add(ResampleFilters.getLanczos3Filter());
		filters.add(ResampleFilters.getBiCubicHighFreqResponse());
		filters.add(ResampleFilters.getBiCubicFilter());
		filters.add(ResampleFilters.getMitchellFilter());
		filters.add(ResampleFilters.getHermiteFilter());
		filters.add(ResampleFilters.getTriangleFilter());
		filters.add(ResampleFilters.getBSplineFilter());
		filters.add(ResampleFilters.getBellFilter());
		filters.add(ResampleFilters.getBoxFilter());

	}

	private int filterIdx;

	private ResampleFilter filter;

	public ResizerNobel as(int filterI) {
		return new ResizerNobel(filterI);
	}

	public ResizerNobel(int filterI) {
		if (filterI < 0) return ;
		this.filterIdx = filterI;
		this.filter = filters.get(filterI);
	}

	@Override
	public BufferedImage resize(BufferedImage src, int w, int h) {
		ResampleOp  resampleOp = new ResampleOp (w,h);
		resampleOp.setFilter(filter);
		resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.None);
		BufferedImage rescaledTomato = resampleOp.filter(src, null);
		return rescaledTomato;
	}

	@Override
	public String name() {
		return filter.getClass().getSimpleName();
	}

	public int getMaxFilters() {
		return 9;
	}

	@Override
	public int current() {
		return filterIdx;
	}

	public static void main(String[] args) {
		int max = ResizerNobel.instance.getMaxFilters();
		for (int i = 0; i < max; i++) {
			ResizerBase rs = new ResizerNobel(i);
			System.out.println("["+i+"]="+rs.name());
		}

	}

	@Override
	public String toString() {
		return String.format("ResizerNobel [%s]", name());
	}
}
