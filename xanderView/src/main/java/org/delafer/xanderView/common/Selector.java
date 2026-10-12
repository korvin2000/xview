package org.delafer.xanderView.common;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import net.j7.commons.utils.RandomUtil;

public class Selector {

	private final int total, required;
	private float gape;
	private float pos = 0f;
	private IntArrayList sets;

	private IntHashSet retLst;

	public Selector(int total, int required) {
		this.total = total;
		this.required = required;
		this.gape = ((total - (float)required) / (1f + required));
		if (this.gape < 0.0001f) this.gape = 0.0f;
		reset();
	}

	public void reset() {
		pos = 0;
		sets = null;
		if (gape > 0.0f) {
			retLst = new IntHashSet(required);
		}
	}

	public int next() {
		pos += gape;
		float r = pos;
		pos += 1.0f;
		int ri = Math.round(r);
		if (ri < total) {
			if (null != retLst) retLst.add(ri);
			return ri;
		}
		if (gape < 0.0001f) return -1;
		if (null == sets) {
			sets = new IntArrayList(total-retLst.size());
			for (int i = 0; i < total; i++) {
				if (!retLst.contains(i)) sets.add(i);
			}
		}
		if (sets.size() > 0) {
			int at = RandomUtil.getRandomInt(0, sets.size()-1);
			ri = sets.get(at);
			sets.remove(at);
			return ri;
		}
		return -1;
	}

//	public static void main(String[] args) {
//		int req =13;
//		Selector selector = new Selector(1024, req);
//		for (int i = 0; i < req; i++) {
//			System.out.println(selector.next());
//		}
//	}

	public static float getElements(int elements) {
		if (elements > 8000) return 11f;
		if (elements > 4000) return 7f;
		if (elements > 1000) return 4.5f;
		if (elements > 500) return 3f;
		if (elements > 300) return 2f;
		if (elements > 200) return 1.67f;
		if (elements > 120) return 1.33f;
		return 1.0f;
	}

	public static int getElements(int minimum, int elements) {
		if (minimum <= 0) return 0;
		float factor = getElements(elements);
		return Math.round(factor * minimum);
	}
}
