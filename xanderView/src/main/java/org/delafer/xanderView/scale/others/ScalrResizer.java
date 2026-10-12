package org.delafer.xanderView.scale.others;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.List;

import org.delafer.xanderView.scale.ResizerBase;
import org.delafer.xanderView.scale.deprecated.Scalr;
import org.delafer.xanderView.scale.deprecated.Scalr.Method;

public class ScalrResizer extends ResizerBase {

	public static final ResizerBase instance = new ScalrResizer(-1);


	static Object[] INTERPOLATION = new Object[] {Scalr.Method.SPEED, Scalr.Method.AUTOMATIC, Scalr.Method.BALANCED, Scalr.Method.QUALITY, Scalr.Method.ULTRA_QUALITY};
	static Object[] ANTIALIASING = new Object[] { Scalr.OP_ANTIALIAS,  null};

	static List<Object[]> filters = new ArrayList<Object[]>();
	static {

			for (Object nextA : ANTIALIASING) {
					for (Object nextI : INTERPOLATION) {
						filters.add(new Object[]{nextI, nextA});
			}

		}
		System.out.println(filters.size());
	}



	private Object[] filterCfg;
	private int filterIdx;

	public ScalrResizer as(int filterI) {
		return new ScalrResizer(filterI);
	}

	public ScalrResizer(int filterI) {
		if (filterI < 0) return ;
		//1-24
		this.filterIdx = filterI;
		this.filterCfg = filters.get(filterI);
	}

	@Override
	public BufferedImage resize(BufferedImage src, int w, int h) {
		return  Scalr.resize(src, (Method)filterCfg[0], Scalr.Mode.FIT_EXACT, w, h, (BufferedImageOp)filterCfg[1]);
	}

	public int getMaxFilters() {
		return 10;
	}

	@Override
	public String name() {
		return "Scalr_"+filterCfg[0]+(filterCfg[1] == Scalr.OP_ANTIALIAS ? "_AA" : "");
	}


	public static void main(String[] args) {
//		 ResizerBase r = ScalrResizer.instance;
		ResizerBase r = new ScalrResizer(6);
		ResizerBase b = r;
		System.out.println(r.name());
	}

	@Override
	public int current() {
		return filterIdx;
	}

	@Override
	public String toString() {
		return String.format("ScalrResizer [%s]", this.name());
	}
}
